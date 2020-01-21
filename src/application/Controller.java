package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Controller {
	@FXML
	Button export;
	@FXML
	Button getFixes;
	@FXML
	Button chooser;
	@FXML
	TextArea logger;
	@FXML
	TextField trackid;
	@FXML
	TextField teamid;
	String path;
	CostumLocation[] loc;

	@FXML
	public void getGpsFixes() {
		int team = Integer.parseInt(teamid.getText());
		int track = Integer.parseInt(trackid.getText());
		logger.appendText("Getting GPS Fixes\n");
		try {

			Client client = Client.create();

			WebResource webResource = client
					.resource("http://pi-bo.dd-dns.de:8080/LM-Server/api/v2/data?teamid=" + team + "&trackid=" + track);

			ClientResponse response = webResource.get(ClientResponse.class);
			;

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}

			String res = response.getEntity(String.class);
			ObjectMapper mapper = new ObjectMapper();
			loc = mapper.readValue(res, CostumLocation[].class);

			for (CostumLocation x : loc) {
				logger.appendText("Lattitude : " + x.latitude + "-Timestamp: " + x.timestamp + "\n");
			}

		} catch (Exception e) {

			e.printStackTrace();

		}
	}

	@FXML
	public void exportKMLDatei() {
		String in = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"+
				"<Document>";
		for (CostumLocation x : loc) {
		 in+="\t<Placemark>\n" +
                 "\t<name>"+x.counter+"</name>\n" +
                 "\t<description>"+x.track+"</description>\n" +
                 "\t<Point>\n" +
                 "\t\t<coordinates>"+x.longitude+","+x.latitude+","+x.altitude+ "</coordinates>\n" +
                 "\t</Point>\n" +
                 "\t</Placemark>\n";
		}
		in+="</Document>\n</kml>";

		try {
			FileWriter writer = new FileWriter(path + "/locs.kml");
			writer.write(in);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.appendText("KML-Datei an : " + path + "exportiert\n");
	}

	@FXML
	public void chosePath() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(new File("src"));
		Window primaryStage = new Stage();
		File selectedDir = dirChooser.showDialog(primaryStage);
		if (selectedDir != null) {
			path = selectedDir.getAbsolutePath();
			logger.appendText("selected Path : " + path + "\n");
		}
	}

}
