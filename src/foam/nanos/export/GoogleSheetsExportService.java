// WARNING: GENERATED CODE, DO NOT MODIFY BY HAND!
// SOURCE: foam2/src/foam/nanos/export/GoogleSheetsExportService.js
package foam.nanos.export;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import foam.nanos.export.GoogleSheetsPropertyMetadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;

import static com.itextpdf.html2pdf.html.AttributeConstants.APPLICATION_NAME;

public class GoogleSheetsExportService extends foam.core.AbstractFObject implements foam.nanos.export.GoogleSheetsExport {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.DRIVE_FILE);
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  public com.google.api.client.auth.oauth2.Credential getCredentials(com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT) throws java.io.IOException {
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(System.getProperty("NANOPAY_HOME") + "/credentials.json")));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
              HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
              .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(System.getProperty("NANOPAY_HOME"))))
              .setAccessType("offline")
              .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(64342).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  public String createSheet(Object obj, Object metadataObj) {
    
    if( !obj.getClass().isArray())
      return "";

    try {
      List<List<Object>> listOfValues = new ArrayList<>();
      Object[] metadataArr = (Object[])metadataObj;
      GoogleSheetsPropertyMetadata[] metadata = new GoogleSheetsPropertyMetadata[metadataArr.length];

      for(int i = 0; i < metadata.length; i++) {
        metadata[i] = (GoogleSheetsPropertyMetadata)metadataArr[i];
      }

      Object[] arr = (Object[]) obj;
      for ( Object v : arr ) {
        listOfValues.add(Arrays.asList((Object[])v));
      }

      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

      Spreadsheet st = new Spreadsheet().setProperties(
        new SpreadsheetProperties().setTitle("My Spreadsheet"));


      List<ValueRange> data = new ArrayList<>();
      data.add(new ValueRange()
        .setRange("A1")
        .setValues(listOfValues));

      BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
        .setValueInputOption("USER_ENTERED")
        .setData(data);

      Spreadsheet response = service.spreadsheets().create(st)
        .execute();

      String url = response.getSpreadsheetUrl();
      BatchUpdateValuesResponse batchResult = service.spreadsheets().values()
        .batchUpdate(response.getSpreadsheetId(), batchBody)
        .execute();

      Request fontSizeRequest = new Request().setRepeatCell(new RepeatCellRequest()
        .setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setFontSize(10))))
        .setRange(new GridRange().setEndRowIndex(listOfValues.size() + 1))
        .setFields("userEnteredFormat.textFormat.fontSize"));

      Request fontFamilyRequest = new Request().setRepeatCell(new RepeatCellRequest()
        .setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setFontFamily("Roboto"))))
        .setRange(new GridRange().setEndRowIndex(listOfValues.size() + 1))
        .setFields("userEnteredFormat.textFormat.fontFamily"));

      Request titleBoldRequest = new Request().setRepeatCell(new RepeatCellRequest()
        .setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setBold(true))))
        .setRange(new GridRange().setEndRowIndex(1))
        .setFields("userEnteredFormat.textFormat.bold"));
      
      Request alternatingColors = new Request().setAddBanding(new AddBandingRequest()
        .setBandedRange(new BandedRange().setRange(new GridRange().setEndRowIndex(listOfValues.size()).setEndColumnIndex(listOfValues.get(0).size())).setRowProperties(
          new BandingProperties()
            .setHeaderColor(new Color().setRed(0.643f).setGreen(0.761f).setBlue(0.957f))
            .setFirstBandColor(new Color().setRed(1f).setGreen(1f).setBlue(1f))
            .setSecondBandColor(new Color().setRed(0.91f).setGreen(0.941f).setBlue(0.996f))//232,240,254
        )));

      List<Request> requests = new ArrayList<Request>(){{
        add(titleBoldRequest);
        add(fontSizeRequest);
        add(fontFamilyRequest);
        add(alternatingColors);
      }};

      requests.add(new Request().setAutoResizeDimensions(new AutoResizeDimensionsRequest().setDimensions(new DimensionRange().setSheetId(0).setDimension("COLUMNS").setEndIndex(metadata.length))));

      for(int i = 0; i < metadata.length; i++) {
        if(metadata[i].getColumnWidth() > 0)
          requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest().setRange(new DimensionRange().setSheetId(0).setDimension("COLUMNS").setStartIndex(i).setEndIndex(i+1)).setProperties(new DimensionProperties().setPixelSize(metadata[i].getColumnWidth())).setFields("pixelSize")));
        if(metadata[i].getCellType().equals("String"))
          continue;
        if( metadata[i].getPattern().isEmpty())
          requests.add(new Request().setRepeatCell(
            new RepeatCellRequest()
          .setCell(new CellData().setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType(metadata[i].getCellType()))))
          .setRange(new GridRange().setStartRowIndex(1).setStartColumnIndex(i).setEndColumnIndex(i+1))
          .setFields("userEnteredFormat.numberFormat")
          ));
        else
          requests.add(new Request().setRepeatCell(
            new RepeatCellRequest()
              .setCell(new CellData().setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType(metadata[i].getCellType()).setPattern(metadata[i].getPattern()))))
              .setRange(new GridRange().setStartRowIndex(1).setStartColumnIndex(i).setEndColumnIndex(i+1))
              .setFields("userEnteredFormat.numberFormat")
          ));

        if(metadata[i].getCellType().equals("CURRENCY")) {
          for(int j = 0; j < metadata[i].getPerValuePatternSpecificValues().length; j++) {
            if(metadata[i].getPerValuePatternSpecificValues()[j].equals("CAD"))
              continue;
            requests.add(new Request().setRepeatCell(
              new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType(metadata[i].getCellType()).setPattern("\"$\"###,###,##\"" + metadata[i].getPerValuePatternSpecificValues()[j] + "\""))))
                .setRange(new GridRange().setStartColumnIndex(i).setEndColumnIndex(i+1).setStartRowIndex(j+1).setEndRowIndex(j+2))
                .setFields("userEnteredFormat.numberFormat")
            ));
          }
        }
        if(metadata[i].getCellType().equals("DATE_TIME")) {
          for(int j = 0; j < metadata[i].getPerValuePatternSpecificValues().length; j++) {
            requests.add(new Request().setRepeatCell(
              new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType(metadata[i].getCellType()).setPattern("ddd mmm d yyyy hh/mm/ss\" " + metadata[i].getPerValuePatternSpecificValues()[j] + "\""))))
                .setRange(new GridRange().setStartColumnIndex(i).setEndColumnIndex(i+1).setStartRowIndex(j+1).setEndRowIndex(j+2))
                .setFields("userEnteredFormat.numberFormat")
            ));
          }
        }
        if(metadata[i].getCellType().equals("DATE_TIME")) {
          for(int j = 0; j < metadata[i].getPerValuePatternSpecificValues().length; j++) {
            requests.add(new Request().setRepeatCell(
              new RepeatCellRequest()
                .setCell(new CellData().setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType(metadata[i].getCellType()).setPattern("hh/mm/ss\" " + metadata[i].getPerValuePatternSpecificValues()[j] + "\""))))
                .setRange(new GridRange().setStartColumnIndex(i).setEndColumnIndex(i+1).setStartRowIndex(j+1).setEndRowIndex(j+2))
                .setFields("userEnteredFormat.numberFormat")
            ));
          }
        }
      }

      BatchUpdateSpreadsheetRequest r = new BatchUpdateSpreadsheetRequest().setRequests(requests);

      BatchUpdateSpreadsheetResponse resp = service.spreadsheets()
        .batchUpdate(response.getSpreadsheetId(), r)
        .execute();

      System.out.print(url);
      return url;
    } catch(Exception e) {
        System.out.print(e);
        return "";
    }
          
  }
}
