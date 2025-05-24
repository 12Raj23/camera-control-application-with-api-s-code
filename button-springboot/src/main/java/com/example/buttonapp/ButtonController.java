package com.example.buttonapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.buttonapp.dto.LoginRequest;
import com.example.buttonapp.dto.RegisterRequest;
import com.example.buttonapp.model.User;
import com.example.buttonapp.repo.UserRepository;
import com.example.buttonapp.service.UserService;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ButtonController {

    private static final Logger logger = LoggerFactory.getLogger(ButtonController.class);
    
    

    private String lastConnectedIp;
    private int lastConnectedPort;

    // DTO for server connection details
    public static class ServerDetails {
        private String ipAddress;
        private String port;

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }
    }

    

    // Endpoint to connect and test server connection
    @PostMapping("/connect")
    public ResponseEntity<String> connectServer(@RequestBody ServerDetails serverDetails) {
        try {
            lastConnectedIp = serverDetails.getIpAddress();
            lastConnectedPort = Integer.parseInt(serverDetails.getPort());

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(lastConnectedIp, lastConnectedPort), 3000);
            }

            logger.info("Successfully connected to {}:{}", lastConnectedIp, lastConnectedPort);
            return ResponseEntity.ok("Connection established successfully");
        } catch (Exception e) {
            logger.error("Connection failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to connect: " + e.getMessage());
        }
    }

    // DTO for button command
    public static class CommandData {
        private String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    // Endpoint to send button command to connected server
    @PostMapping("/button1")
    public ResponseEntity<String> handleButtonCommand(@RequestBody CommandData commandData) {
        String command = commandData.getValue();
        logger.info("Received button command: {}", command);

        if (lastConnectedIp == null || lastConnectedPort == 0) {
            return ResponseEntity.status(400).body("No active server connection. Please connect first.");
        }

        boolean success = sendTcpCommand(lastConnectedIp, lastConnectedPort, command);
        if (success) {
            return ResponseEntity.ok("Command sent: " + command);
        } else {
            return ResponseEntity.status(500).body("Failed to send command.");
        }
    }

    private boolean sendTcpCommand(String ip, int port, String message) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 3000);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(message);
            logger.info("Command sent to {}:{} => {}", ip, port, message);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send TCP command: {}", e.getMessage());
            return false;
        }
    }
}
