package org.example.view;//view/view.ConsoleUI.java

import java.util.Scanner;

public class ConsoleUI {
    Scanner scanner = new Scanner(System.in);

    public String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

}