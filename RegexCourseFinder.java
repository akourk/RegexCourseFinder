// Author       :   Alex Kourkoumelis
// Date         :   1/24/2018
// Title        :   Regex Course Finder
// Description  :   Uses Regex to navigate through the Bellevue College
//              :   website to find course info. Takes several steps of
//              :   inputs and sifts through source code and appends the URL

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

public class RegexCourseFinder {

    public static ArrayList<String> subjects = new ArrayList<>();
    public static ArrayList<String> classes = new ArrayList<>();
    public static ArrayList<String> itemNumber = new ArrayList<>();
    public static ArrayList<String> instructors = new ArrayList<>();
    public static ArrayList<String> meets = new ArrayList<>();
    public static String appendURL;

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        startLoop();
    }

    public static void startLoop() throws IOException {
        String usePattern = "";
        appendURL = "https://www.bellevuecollege.edu/classes/";

        Scanner console = new Scanner(System.in);

        usePattern += quarter(console);

        usePattern += year(console);

        appendURL += usePattern;
        appendURL += "?letter=" + program(console);

        URL url = new URL(appendURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String input = "";
        String text = "";

        while((input = in.readLine())!=null) {
            text += input + "\n";
        }

        matchSubjects(usePattern, text);
        listSubjects(console);
        listClasses(console);
        System.out.println(classes);
        selectClasses(console);
    }

    public static void getTimes(String subChunk) {

        String usePattern = "abbr title=\"[\\s\\S]*?</abbr>";
        Pattern pattern = Pattern.compile(usePattern);
        Matcher matcher = pattern.matcher(subChunk);
        String temp = "";
        while(matcher.find()){
            temp = matcher.group();
            String[] split = temp.split("\"", 3);
            temp = split[1];
            meets.add(temp);
        }

    }

    public static void siftChunks(String chunk, String specificClass) {
        int iterator = 0;
        String usePattern;
        String temp;
        usePattern = "Item number: </span>[0-9]*</span>";
        Pattern pattern = Pattern.compile(usePattern);
        Matcher matcher = pattern.matcher(chunk);
        while(matcher.find()){
            temp = matcher.group().substring(20, 24);
            itemNumber.add(temp);
            iterator++;
        }

        usePattern = "SearchString=[a-zA-Z]*(.*)[a-zA-Z]*\">";
        pattern = Pattern.compile(usePattern);
        matcher = pattern.matcher(chunk);

        while(matcher.find()){
            temp = matcher.group().substring(13, matcher.group().length() -2);
            temp = temp.replaceAll("\\+", " ");
            instructors.add(temp);
        }

        usePattern = "courseTitle\">(.*)</span>";
        pattern = Pattern.compile(usePattern);
        matcher = pattern.matcher(chunk);
        String courseTitle = "";
        while(matcher.find()){
            courseTitle = matcher.group();
            courseTitle = courseTitle.substring(13, courseTitle.length() - 7);
        }


        usePattern = "meets\">[\\s\\S]*?</ul>";
        pattern = Pattern.compile(usePattern);
        matcher = pattern.matcher(chunk);
        while(matcher.find()){
            temp = matcher.group();
            if (temp.contains("online")) {
                meets.add("Online");
            } else {
                getTimes(temp);
            }
        }


        for(int i = 0; i < iterator; i++) {
            System.out.println("######################################");
            System.out.println("Title: " + courseTitle);
            System.out.println("Code: " + specificClass);
            System.out.println("Item#: " + itemNumber.get(i));
            System.out.println("Instructor: " + instructors.get(i));
            System.out.println("Days: " + meets.get(i));
            System.out.println("######################################");
        }
    }

    public static void extractClassChunks(String specificClass) throws IOException {
        URL url = new URL(appendURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String input, text = "", usePattern = "";

        while((input = in.readLine())!=null) {
            text += input + "\n";
        }

        String nextClass = "";
        for(int i = 0; i < classes.size(); i++) {
            if (classes.get(i).contains(specificClass)) {
                if (i == classes.size() - 1) {
                    nextClass = "registration";
                } else {
                    nextClass = classes.get(i+1);
                }
            }
        }

        usePattern = specificClass + "</span>[\\s\\S]*?" + nextClass;
        Pattern pattern = Pattern.compile(usePattern);
        Matcher matcher = pattern.matcher(text);
        //String output;
        while(matcher.find()) {
//            System.out.println(matcher.group());
//            System.out.println();
//            System.out.println("end of finder");
            siftChunks(matcher.group(), specificClass);
        }

    }

    public static void selectClasses(Scanner console) throws IOException {
        System.out.println("Enter the class: ");
        String specificClass = console.nextLine();

        boolean checker = false;

        for (int i = 0; i < classes.size(); i++) {
            if(classes.get(i).toLowerCase().contains(specificClass.toLowerCase())) {

                //System.out.println(classes.get(i) + " matches " + specificClass);

                specificClass = classes.get(i);
                checker = true;
            }
        }
        if (checker) {
            extractClassChunks(specificClass);
        } else {
            System.out.println("Sorry, that doesn't seem to match any class names.");
            System.out.println("List of classes are: ");
            System.out.println(classes);
            selectClasses(console);
        }

    }

    public static void listClasses(Scanner console) throws IOException {

        URL url = new URL(appendURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String input, text = "", usePattern = "";

        while((input = in.readLine())!=null) {
            text += input + "\n";
        }

        usePattern += "courseID\">[A-Z]*\\s[0-9]*</";

        Pattern pattern = Pattern.compile(usePattern);
        Matcher matcher = pattern.matcher(text);
        String output;
        while(matcher.find()) {
            output = matcher.group();
            output = output.substring(10, output.length() - 2);
            classes.add(output);
        }

    }

    public static void listSubjects(Scanner console) throws IOException {
        System.out.println("Enter the program's name: ");
        console.nextLine();
        String specificPrograms = console.nextLine();

        boolean checker = false;
        //must iterate backwards because string can have 2 matches
        //and we want to prioritize the abbreviation
        for (int i = subjects.size() - 1; i >= 0; i--) {
            if(subjects.get(i).toLowerCase().contains(specificPrograms.toLowerCase())) {

                //System.out.println(specificPrograms + " matches " + subjects.get(i));
                System.out.println();

                //if abbrev is typed, i % 2 = 0, if full name is typed i % 2 = 1
                //we want to appendURL with abbrev, so we take i-1.
                if(i % 2 == 1) {
                    specificPrograms = subjects.get(i-1);
                } else {
                    specificPrograms = subjects.get(i);
                }
                checker = true;
            }
        }
        if (checker) {
            appendURL = appendURL.substring(0, appendURL.length() - 9);
            appendURL += "/" + specificPrograms;
        } else {
            System.out.println("Sorry, that doesn't seem to match any program names.");
            System.out.println("List of programs are: ");
            System.out.println(subjects);
            listSubjects(console);
        }
}

    public static void matchSubjects(String usePattern, String text) {
        System.out.println("Programs:");

        usePattern += "/[A-Z]*\">(.*)</a>\\s[(](.*)[)]";

        Pattern pattern = Pattern.compile(usePattern);
        Matcher matcher = pattern.matcher(text);

        String outputAbbrev = "";
        String outputName = "";
        while(matcher.find()) {
            outputAbbrev += matcher.group(1) + " (" + matcher.group(2) + ")";
            outputAbbrev = outputAbbrev.replaceAll("&amp;", "&");
            System.out.println(outputAbbrev);

            String[] splitName = outputAbbrev.split("\\(", 2);
            outputName = splitName[0].substring(0, splitName[0].length() -1);


            if (outputAbbrev.contains(",")) {
                outputAbbrev = outputAbbrev.replaceAll(",", "\\)");
            }
            if (outputAbbrev.contains(")")) {
                outputAbbrev = outputAbbrev.replaceAll("\\)", "(");
            }
            String[] splitAbbrev = outputAbbrev.split("\\(", 3);

            outputAbbrev = splitAbbrev[1].substring(0, splitAbbrev[1].length());

            subjects.add(outputAbbrev);
            subjects.add(outputName);

            outputAbbrev = "";
            outputName = "";
        }
        System.out.println();
    }

    public static String quarter(Scanner console) {
        String quarter;
        System.out.println("What quarter would you like to search? ");
        System.out.println("Winter, Spring, Summer, or Fall?");
        quarter = console.next();
        return quarter;
    }

    public static String year(Scanner console) {
        String year;
        System.out.println("What year would you like to search?");
        year = console.next();
        return year;
    }

    public static String program(Scanner console) {
        String program;
        System.out.println("What is the first letter of the program you want to search?");
        program = console.next();
        return program;
    }
}
