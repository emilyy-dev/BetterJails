package me.fefo.betterjails.common.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import me.fefo.betterjails.common.events.BJEvent;

public class SQLite {
    private Connection conn;
    private Statement stmt;

    public SQLite(String dbName) {
        Thread thread = new Thread (() -> {
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
                stmt = conn.createStatement();
                System.out.println("SQLite connected successfully!");
                //setupTables();
                //checkIfColumnsExists();
            } catch (/*ClassNotFoundException | */SQLException e) {
                System.out.println("SQLite connection failed!");
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void log(LogTypes type, BJEvent event) {
        switch (type) {
            case CreateJail:
                logCreateJail(event);
                break;
            case DeleteJail:
                logDeleteJail(event);
                break;
            case CreateCell:
                logCreateCell(event);
                break;
            case DeleteCell:
                logDeleteCell(event);
                break;
            case CancelledJailCreation:
                logCancelledJailCreation(event);
                break;
            case CancelledJailDeletion:
                logCancelledJailDeletion(event);
                break;
            case CancelledCellCreation:
                logCancelledCellCreation(event);
                break;
            case CancelledCellDeletion:
                logCancelledCellDeletion(event);
                break;
            case JailedPlayer:
                logJailedPlayer(event);
                break;
            case FreedPlayer:
                logFreedPlayer(event);
                break;
        }
    }

    private void logFreedPlayer(BJEvent event) {
        System.out.println("Player freed yay");
    }

    private void logJailedPlayer(BJEvent event) {
        System.out.println("Player jailed.");
    }

    private void logCancelledCellDeletion(BJEvent event) {
        System.out.println("Cell deletion event cancelled!");
    }

    private void logCancelledCellCreation(BJEvent event) {
        System.out.println("Cell creation event cancelled!");
    }

    private void logCancelledJailDeletion(BJEvent event) {
        System.out.println("Jail deletion event cancelled!");
    }

    private void logCancelledJailCreation(BJEvent event) {
        System.out.println("Jail creatione vent cancelled!");
    }

    private void logDeleteCell(BJEvent event) {
        System.out.println("Cell deleted!");
    }

    private void logCreateCell(BJEvent event) {
        System.out.println("Cell created!");
    }

    private void logDeleteJail(BJEvent event) {
        System.out.println("Jail deleted!");
    }

    private void logCreateJail(BJEvent event) {
        System.out.println("Jail created!");
    }
}
