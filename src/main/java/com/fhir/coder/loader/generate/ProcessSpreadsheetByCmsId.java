package com.fhir.coder.loader.generate;

import ca.uhn.fhir.context.FhirContext;
import com.fhir.coder.loader.util.LoaderUtil;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.SAXHelper;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static com.fhir.coder.loader.SystemURLMappings.shortHandToSystemUrl;

/**
 * Process the VSAC spreadsheet and produce ValueSet(s):
 *
 * TODO: Make this process a part of the maven resource generation, that way previous files will automatically get deleted during clean.
 */
public class ProcessSpreadsheetByCmsId {

    public static void main(String[] args) throws IOException {
        //TODO: need to get mvn generate-resources working
        deleteDir(new File("target/gen-resources/"));

        //identify all spreadsheets
        getAllSpreadsheets();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
                System.out.println("deleted: " + children[i]);
            }
        }
        return dir.delete();
    }

    private static void getAllSpreadsheets() throws IOException {
        Path dir = Paths.get("src/main/resources");
        Files.walk(dir).forEach(path -> {
            try {
                processFile(path.toFile());
            } catch (OpenXML4JException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        });
    }

    private static void processFile(File entry) throws OpenXML4JException, IOException, SAXException {
        String filePath = entry.getPath();
        if (filePath.endsWith(".xlsx") && filePath.contains("ep_ec_eh_cms_")) {
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            String[] split = filePath.split(pattern);
            String reportYear = split[3];
            OPCPackage opcPkg = OPCPackage.open(filePath, PackageAccess.READ);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPkg);
            XSSFReader xssfReader = new XSSFReader(opcPkg);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator sheetsData = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (sheetsData.hasNext()) {
                InputStream stream = sheetsData.next();
                //CMS104v10, CMS105v10, CMS108v10, etc...
                String[] reportAndVersion = extractReportAndVersion(sheetsData.getSheetName());
                processSheet(styles, strings, getHandler(reportYear, reportAndVersion[0], reportAndVersion[1]), stream);
                stream.close();
            }
        }
    }
    public static String[] extractReportAndVersion(String worksheetName) {
        System.out.println("worksheet: " + worksheetName);
        String[] reportAndVersion = worksheetName.split("v");
        System.out.println("report: " + reportAndVersion[0] + ", version: " + reportAndVersion[1]);
        return reportAndVersion;
    }

    public static void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, XSSFBSheetHandler.SheetContentsHandler sheetHandler,
                                    InputStream sheetInputStream) throws IOException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader(new XmlOptions());
            ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    private static XSSFBSheetHandler.SheetContentsHandler getHandler(String reportYear, String reportName, String revision) {
        return new XSSFBSheetHandler.SheetContentsHandler() {
            private String path = "target/gen-resources/" + reportYear + "/" + reportName + "/" + revision + "/";
            private boolean firstCellOfRow = false;
            private int currentRow = -1;
            private int currentCol = -1;

            // Maps column Letter name to its value.
            // Does not contain key-value pair if cell value is null for
            // currently
            // processed column and row.
            private Map<String, String> rowValues;
            private boolean titleRowFound = false;

            private String currentValueSetOID = "";
            private ValueSet valueSet;

            @Override
            public void startRow(int rowNum) {
                // Prepare for this row
                firstCellOfRow = true;
                currentRow = rowNum;
                currentCol = -1;
                rowValues = new HashMap<>();
            }

            @Override
            public void endRow(int rowNum) {
                if (rowValues.keySet().size() == 0) {
                    //Skipping calling rowRead() because of empty row");
                } else {
                    if (!titleRowFound) {
                        titleRowFound = identifyTitleRow();
                    } else {
                        try {
                            processChildRow();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            private void processChildRow() throws IOException {
                //need to delimit when processing a different valueset is being processed
                if (!rowValues.get("D").equals(currentValueSetOID)) {
                    finishValueSet();
                    constructParentValueSet();
                }
                valueSet.getCompose().addInclude()
                        .setSystem((shortHandToSystemUrl.containsKey(rowValues.get("N")))?shortHandToSystemUrl.get(rowValues.get("N")):rowValues.get("N"))
                        .setVersion(rowValues.get("P"))
                        .addConcept(new ValueSet.ConceptReferenceComponent().setCode(rowValues.get("L")).setDisplay(rowValues.get("M")));

//                        .addFilter().setProperty(-298878168, rowValues.get("B"), new CodeableConcept().setText(rowValues.get("B")).setId(rowValues.get("A")));
//                        .setOp(ValueSet.FilterOperator.IN)
//                        .setValue(rowValues.get("A"));
            }

            private void constructParentValueSet() {
                currentValueSetOID = rowValues.get("D");
                valueSet = new ValueSet()
                        .setName(rowValues.get("C"))
                        .setDescription(rowValues.get("H"))
                        .setVersion(rowValues.get("F"))
                        .setUrl("urn:oid:" + rowValues.get("D"))
                        .setStatus(Enumerations.PublicationStatus.ACTIVE);

                //TODO: consider compounding H, I, J, K inside the <text><div> to build the narrative
                valueSet.setText(new Narrative(new Narrative.NarrativeStatusEnumFactory().fromType(new StringType("extensions")), new XhtmlDocument().div()));
            }

            private boolean identifyTitleRow() {
                boolean titleRow = false;
                if (rowValues.containsKey("A") && rowValues.get("A").equals("CMS ID") &&
                        rowValues.containsKey("B") && rowValues.get("B").equals("NQF Number") &&
                        rowValues.containsKey("C") && rowValues.get("C").equals("Value Set Name") &&
                        rowValues.containsKey("D") && rowValues.get("D").equals("Value Set OID") &&
                        rowValues.containsKey("E") && rowValues.get("E").equals("QDM Category") &&
                        rowValues.containsKey("F") && rowValues.get("F").equals("Definition Version") &&
                        rowValues.containsKey("G") && rowValues.get("G").equals("Expansion Version") &&
                        rowValues.containsKey("H") && rowValues.get("H").equals("Purpose: Clinical Focus") &&
                        rowValues.containsKey("I") && rowValues.get("I").equals("Purpose: Data Element Scope") &&
                        rowValues.containsKey("J") && rowValues.get("J").equals("Purpose: Inclusion Criteria") &&
                        rowValues.containsKey("K") && rowValues.get("K").equals("Purpose: Exclusion Criteria") &&
                        rowValues.containsKey("L") && rowValues.get("L").equals("Code") &&
                        rowValues.containsKey("M") && rowValues.get("M").equals("Description") &&
                        rowValues.containsKey("N") && rowValues.get("N").equals("Code System") &&
                        rowValues.containsKey("O") && rowValues.get("O").equals("Code System OID") &&
                        rowValues.containsKey("P") && rowValues.get("P").equals("Code System Version") &&
                        rowValues.containsKey("Q") && rowValues.get("Q").equals("Expansion ID")) {
                    titleRow = true;
                }
                return titleRow;
            }

            @Override
            public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                if (firstCellOfRow) {
                    firstCellOfRow = false;
                }

                // gracefully handle missing CellRef here in a similar way as XSSFCell does
                if (cellReference == null) {
                    cellReference = new CellAddress(currentRow, currentCol).formatAsString();
                }

                // Did we miss any cells?
                int thisCol = (new CellReference(cellReference)).getCol();
                currentCol = thisCol;

                cellReference = cellReference.replaceAll("\\d","");
                rowValues.put(cellReference, formattedValue);
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {
            }

            public void finishValueSet() throws IOException {
                //if we're at the beginning of the spreadsheet
                if (valueSet == null) {
                    return;
                }
//                createIndexFile();
                createValueSetFiles();
                valueSet = null;
            }

            private void createIndexFile() throws IOException {
                String filename = valueSet.getUrl().substring(8) + ".idx";
                File f = new File(path, filename);
                f.getParentFile().mkdirs();
                f.createNewFile();
                Map<String, String> map = new HashMap<>();
                BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
                for (ValueSet.ConceptSetComponent include : valueSet.getCompose().getInclude()) {
                    if(map.containsKey(include.getSystem())) {
                        map.put(include.getSystem(), map.get(include.getSystem()) + "|" + include.getConcept().get(0).getCode());
                    } else {
                        map.put(include.getSystem(), "|" + include.getConcept().get(0).getCode());
                    }
                }
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + "|\n");
                }
                writer.close();
            }

            //TODO: might want to also generate xml to support external projects
            private void createValueSetFiles() throws IOException {
                String filename = valueSet.getUrl().substring(8) + ".json";
                File f = new File(path, filename);
                //some of the valuesets downloaded may not be ordered, so merge prior subsets
                if (f.exists()) {
                    String relativePath = "target/gen-resources/" + reportYear + "/" + filename;
                    System.out.println("exists: " + relativePath);
                    ValueSet existingValueSet = LoaderUtil.loadResourceFromFile(relativePath);
                    for (ValueSet.ConceptSetComponent include : existingValueSet.getCompose().getInclude()) {
                        valueSet.getCompose().addInclude(include);
                    }
                } else {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
                writer.append(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(valueSet));
                writer.close();
            }

            @Override
            public void hyperlinkCell(String s, String s1, String s2, String s3, XSSFComment xssfComment) {
            }
        };
    }
}
