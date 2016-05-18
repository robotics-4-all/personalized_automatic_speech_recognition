package org.dictionary;

import org.corpus.Word;
import org.corpus.WordSequence;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;


public class Dictionary {
    public static Dictionary createFromFile(File file) throws FileNotFoundException {
        Hashtable<String, String> wordsToPhonesTable = new Hashtable<String, String>();

        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();

            int indexOfSeparation = line.indexOf(" ");

            wordsToPhonesTable.put(line.substring(0, indexOfSeparation),
                    line.substring(indexOfSeparation + 1));
        }
        scanner.close();

        return new Dictionary(wordsToPhonesTable);
    }

    private Dictionary(Hashtable<String, String> wordsToPhonesTable) {
        wordsToPhonesTable_ = wordsToPhonesTable;
    }

    public String getPhones(String word){
        if(word.equals("")){
            return "";
        }

        return wordsToPhonesTable_.get(word);
    }

    public String[] getPhones(String[] words){
        if(words.length == 0){
            return new String[] {};
        }

        int numberOfWords = words.length;

        String[] phones = new String[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            phones[i] = getPhones(words[i]);
        }

        return phones;
    }

    public String[] getPhones(WordSequence wordSequence){
        Word[] words = wordSequence.getWords();

        if(words.length == 0){
            return new String[] {};
        }

        int numberOfWords = words.length;

        String[] phones = new String[numberOfWords];
        for(int i = 0;i < numberOfWords;i++){
            phones[i] = getPhones(words[i].getText());
        }

        return phones;
    }

    private final Hashtable<String, String> wordsToPhonesTable_;

}