package learning.qa;
import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import learning.qa.model.Driver;
import learning.qa.model.Glossary;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;
public class FilesParsingTest {

    ClassLoader cl = FilesParsingTest.class.getClassLoader();

    @Test
    void pdfParseTest() throws Exception {
        open("https://junit.org/junit5/docs/current/user-guide/");
        File downloadedPdf = $("a[href='junit-user-guide-5.10.2.pdf']").download();
        PDF content = new PDF(downloadedPdf);
        assertThat(content.text).contains("Sam Brannen");
    }

    @Test
    void xlsParseTest() throws Exception {

        try (InputStream resourceAsStream = cl.getResourceAsStream("table.xlsx")) {
            XLS content = new XLS(resourceAsStream);
            assertThat(content.excel.getSheetAt(0).getRow(0).getCell(1).getStringCellValue().contains("1"));
        }
    }

    @Test
    void csvParseTest() throws Exception {
        try (InputStream resource = cl.getResourceAsStream("learning.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(resource))) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(0)[1]).contains("lesson");
        }
    }

    @Test
    void zipParseTest() throws Exception {
        try (InputStream resource = cl.getResourceAsStream("sample.zip");
             ZipInputStream zis = new ZipInputStream(resource)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                assertThat(entry.getName()).isEqualTo("easy.txt");
            }
        }
    }

    @Test
    void jsonParseTest() throws Exception {
        Gson gson = new Gson();
        try (
                InputStream resource = cl.getResourceAsStream("glossary.json");
                InputStreamReader reader = new InputStreamReader(resource)
        ) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            assertThat(jsonObject.get("title").getAsString()).isEqualTo("example glossary");
            assertThat(jsonObject.get("GlossDiv").getAsJsonObject().get("title").getAsString()).isEqualTo("S");
            assertThat(jsonObject.get("GlossDiv").getAsJsonObject().get("flag").getAsBoolean()).isTrue();
        }
    }

    @Test
    void jsonParseImprovedTest() throws Exception {
        Gson gson = new Gson();
        try (
                InputStream resource = cl.getResourceAsStream("glossary.json");
                InputStreamReader reader = new InputStreamReader(resource)
        ) {
            Glossary jsonObject = gson.fromJson(reader, Glossary.class);
            assertThat(jsonObject.title).isEqualTo("example glossary");
            assertThat(jsonObject.glossDiv.title).isEqualTo("S");
            assertThat(jsonObject.glossDiv.flag).isTrue();
        }
    }

    @Test
    void jsonDriverParseTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (
                InputStream resource = cl.getResourceAsStream("driver.json");
                InputStreamReader reader = new InputStreamReader(resource)
        ) {
            Driver driver = mapper.readValue(reader,Driver.class);
            assertThat(driver.name).isEqualTo("John");
            assertThat(driver.age).isEqualTo(30);
            assertThat(Arrays.toString(driver.cars)).contains("Ford", "BMW", "Fiat");
        }
    }

    @Test
    void zipVariousTypesParseTest() throws Exception {
        try (InputStream resource = cl.getResourceAsStream("archive.zip");
             ZipInputStream zis = new ZipInputStream(resource, Charset.defaultCharset())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case ("CSVdata.csv"):
                        CSVReader reader = new CSVReader(new InputStreamReader(zis));
                        List<String[]> list = reader.readAll();
                        assertThat(list.get(1)[1]).contains("McGinnis");
                        continue;
                    case ("ExcelData.xlsx"):
                        XLS content = new XLS(zis);
                        assertThat(content.excel.getSheetAt(0).getRow(0).getCell(1).getStringCellValue().contains("Country"));
                        continue;
                    case ("PdfData.pdf"):
                        PDF result = new PDF(zis);
                        assertThat(result.text).contains("Пространство имен OPC DA");
                }
            }
        }
    }
}
