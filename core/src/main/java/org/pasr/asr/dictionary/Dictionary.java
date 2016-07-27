package org.pasr.asr.dictionary;

import org.pasr.asr.Configuration;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


public class Dictionary implements Iterable<Map.Entry<String, String>>{
    public Dictionary(){
        wordsToPhonesTable_ = new LinkedHashMap<>();

        unknownWords_ = new ArrayList<>();
    }

    public static Dictionary createFromStream (InputStream inputStream)
        throws FileNotFoundException {

        Dictionary dictionary = new Dictionary();

        Scanner scanner = new Scanner(inputStream);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();

            int indexOfSeparation = line.indexOf(" ");

            dictionary.add(line.substring(0, indexOfSeparation),
                    line.substring(indexOfSeparation + 1));
        }
        scanner.close();

        return dictionary;
    }

    @SuppressWarnings ("WeakerAccess")
    public List<String> getPhones(String string){
        String phones = wordsToPhonesTable_.get(string);

        return phones == null ? null : Arrays.asList(phones.trim().split(" "));
    }

    @SuppressWarnings ("WeakerAccess")
    public List<String> getPhones(Word word){
        return getPhones(word.getText());
    }

    public List<List<String>> getPhones(WordSequence wordSequence){
        ArrayList<List<String>> phones = new ArrayList<>();

        for(Word word : wordSequence){
            phones.add(getPhones(word));
        }

        return phones;
    }

    public Map<String, String> getEntriesByKey(String key){
        return wordsToPhonesTable_.entrySet().stream().
            filter(entry -> entry.getKey().equals(key) ||
                entry.getKey().matches(key + "\\([0-9]+\\)")).
            collect(Collectors.toMap(Map.Entry:: getKey, Map.Entry:: getValue));
    }

    public List<String> getUnknownWords(){
        return unknownWords_;
    }

    public Set<String> getUniqueWords(){
        return wordsToPhonesTable_.keySet().stream().
            filter(entry -> !entry.contains("(")).
            collect(Collectors.toSet());
    }

    /**
     *
     * @param string
     * @param count
     *     The number of words to return
     * @return
     */
    public List<String> fuzzyMatch(String string, int count){
        String[] bestMatches = new String[count];
        double[] bestDistances = new double[count];
        for(int i = 0;i < count;i++){
            bestDistances[i] = Double.POSITIVE_INFINITY;
        }

        for(String word : getUniqueWords()){
            double distance = getLevenshteinDistance(string, word);

            for(int i = 0;i < count;i++){
                if(distance < bestDistances[i]){
                    bestDistances[i] = distance;
                    bestMatches[i] = word;
                }
            }
        }

        return Arrays.asList(bestMatches);
    }

    public List<String> fuzzyMatch(String string){
        return fuzzyMatch(string, 5);
    }

    public List<List<String>> getUnknownWordsFuzzyMatch(){
        return unknownWords_.stream()
            .map(this :: fuzzyMatch)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void add(String key, String value){
        if(!wordsToPhonesTable_.containsKey(key)) {
            wordsToPhonesTable_.put(key, value);
            return;
        }

        int index = 1;
        String currentKey = key + "(" + index + ")";
        while(wordsToPhonesTable_.containsKey(currentKey)){
            // if the given value already exists inside the dictionary, don't add it again
            if(wordsToPhonesTable_.get(currentKey).equals(value)){
                return;
            }

            index++;
            currentKey = key + "(" + index + ")";
        }

        wordsToPhonesTable_.put(currentKey, value);
    }

    public void add(Map.Entry<String, String> entry){
        add(entry.getKey(), entry.getValue());
    }

    public void addAll(Map<String, String> entries){
        for(Map.Entry<String, String> entry : entries.entrySet()){
            add(entry.getKey(), entry.getValue());
        }
    }

    public void addUnknownWord(String word){
        unknownWords_.add(word);
    }

    public void remove(String key){
        if(wordsToPhonesTable_.remove(key) == null){
            return;
        }

        int index = 2;
        while(wordsToPhonesTable_.remove(key + "(" + index + ")") != null){
            index++;
        }
    }

    public static Dictionary getDefaultDictionary() throws FileNotFoundException {
        return Dictionary.createFromStream(new FileInputStream(
            Configuration.getDefaultConfiguration().getDictionaryPath()
        ));
    }

    public void saveToFile(File file) throws FileNotFoundException {
        // Sort the entries of the dictionary based on the key length. This will ensure that
        // "the(1)" is below "the" when the dictionary is saved to the file.
        List<Map.Entry<String, String>> entries = new ArrayList<>(wordsToPhonesTable_.entrySet());

        Collections.sort(entries, (e1, e2) -> e1.getKey().length() - e2.getKey().length());

        PrintWriter printWriter = new PrintWriter(file);
        for (Map.Entry<String, String> entry : entries) {
            printWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        printWriter.close();
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator () {
        return wordsToPhonesTable_.entrySet().iterator();
    }

    private final Map<String, String> wordsToPhonesTable_;
    private final List<String> unknownWords_;

}
