package com.rfidreader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.comm.*;

import org.codehaus.jackson.map.ObjectMapper;

//class for reading RFID scanned tag from SerialPort and send it to server
public class RFIDReader implements SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;

	BufferedReader br;
	InputStream inputStream;
	SerialPort serialPort;

	public static void checkPort() {

		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			System.out.println("port id is: " + portId);
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals("COM3")) {
					// if (portId.getName().equals("/dev/term/a")) {
					System.out.println("COM3 Port is Found");
					RFIDReader reader = new RFIDReader();
				} else {
					System.out.println("COM3 Port is not Found");
				}
			}
		}
	}

	public RFIDReader() {
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
		} catch (PortInUseException e) {
			System.out.println(e);
		}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println(e);
		}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			System.out.println(e);
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println(e);
		}
	}

	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			br = new BufferedReader(new InputStreamReader(inputStream));
			try {
				String inputLine = br.readLine();
				new Thread(new RFIDDataSender(inputLine, "ATTENDANCE REDER", new Date())).start();
			} catch (IOException e) {
				System.out.println(e);
			}
			break;
		}
	}

	private class RFIDDataSender implements Runnable {

		public final String rfid_tage;
		public final String rfid_reader_name;
		public final Date timestamp;

		private RFIDDataSender(String rfid_tage, String rfid_reader_name, Date timestamp) {
			this.rfid_tage = rfid_tage;
			this.rfid_reader_name = rfid_reader_name;
			this.timestamp = timestamp;
		}

		// send scanned RFID tag to server
		@Override
		public void run() {

			System.out.println("Thread Procssing Data\nRFID_TAGE: " + rfid_tage + "\nREDER_NAME: " + rfid_reader_name
					+ "\nTIMESTAMP: " + timestamp);
			String mappStr = null;
			try {
				mappStr = new ObjectMapper().writeValueAsString(this);
				System.out.println(mappStr);
			} catch (IOException e) {

				e.printStackTrace();
			}

			try {
				URL obj = new URL("http://localhost:8080/rfid/post-tag-data");
				HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
				postConnection.setDoInput(true);
				postConnection.setDoOutput(true);
				postConnection.setRequestMethod("POST");
				postConnection.setRequestProperty("Content-Type", "application/json");

				OutputStream os = postConnection.getOutputStream();
				os.write(mappStr.getBytes());
				os.flush();
				os.close();
				int responseCode = postConnection.getResponseCode();
				System.out.println("POST Response Code :  " + responseCode);
				System.out.println("POST Response Message : " + postConnection.getResponseMessage());
				if (responseCode == HttpURLConnection.HTTP_CREATED) {
					// success
					BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					// print result
					System.out.println(response.toString());
				} else {
					System.out.println("POST NOT WORKED");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}