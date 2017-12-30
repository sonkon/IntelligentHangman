
import java.util.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible wordLists from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

    // instance vars

    ArrayList<String> wordLists =  new ArrayList<String>();
    ArrayList<String> active = new ArrayList<String>();
    ArrayList<Character> usedletters = new ArrayList<Character>();

    String pattern = "";
    String guessesMade = "[";

    int guessesTotal = 1;
    int wordLength = 0;
    int origGuesses = 0;
    int numberGuesses = 0;
    int difficulty = 0;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> word, boolean debugOn) {
        for (String w: word) {
            wordLists.add(w);
        }
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases. 
     * Debugging is off.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        for (String w: words) {
            wordLists.add(w);
        }
    }

    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     * @param length The given length to check.
     * @return the number of words in the original Dictionary with the given length
     */
    public int numWords(int length) {
        int num = 0;
        for (int i = 0; i < wordLists.size(); i++){
            if (wordLists.get(i).length() == length){
                num++;
            }
        }
        return num;
    }

    /**
     * Get for a new round of Hangman. Think of a round as a complete game of Hangman.
     * @param wordLen the length of the word to pick this time. numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the player loses the round. numGuesses >= 1
     * @param diff The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        if(wordLen < 0 || numGuesses <= 1){
            throw new IllegalArgumentException("Violation of precondition in prepForRound.");
        }

        // Reset all the variables
        origGuesses = numGuesses;
        wordLength = wordLen;
        numberGuesses = origGuesses;
        active = new ArrayList<String>();
        guessesTotal = 0;

        // Decide which setting to use
        if (diff == HangmanDifficulty.EASY) {
            difficulty = 1;
        } else if (diff == HangmanDifficulty.HARD){
            difficulty = 3;
        } else {
            difficulty = 2;
        }

        // Add words to the active word list
        usedletters = new ArrayList<Character>();
        for (int i = 0; i < wordLists.size(); i++){
            if (wordLists.get(i).length() == wordLength){
                active.add(wordLists.get(i));
            }
        } 
        pattern = "";
        for (int i = 0; i < wordLength; i++){
            pattern += "-";
        }

    }

    /**
     * The number of words still possible (live) based on the guesses so far. Guesses will eliminate possible words.
     * @return the number of words that are still possibilities based on the original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return active.size();
    }

    /**
     * Get the number of wrong guesses the user has left in this round (game) of Hangman.
     * @return the number of wrong guesses the user has left in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return numberGuesses;
    }

    /**
     * Return a String that contains the letters the user has guessed so far during this round.
     * The String is in alphabetical order. The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     * @return a String that contains the letters the user has guessed so far during this round.
     */
    public String getGuessesMade() {
        return guessesMade + "]";
    }

    /**
     * Check the status of a character.
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman, false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        for (char l: usedletters) {
            if (guess == l) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current pattern. The pattern contains '-''s for unrevealed (or guessed)
     * characters and the actual character for "correctly guessed" characters.
     * @return the current pattern.
     */
    public String getPattern() {
        return pattern;
    }

    // pre: !alreadyGuessed(ch)
    // post: return a tree map with the resulting patterns and the number of
    // words in each of the new patterns.
    // the return value is for testing and debugging purposes
    /**
     * Update the game status (pattern, wrong guesses, word list), based on the give
     * guess. 
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if(alreadyGuessed(guess)){
            throw new IllegalStateException("Violation of precondition in makeGuess.");
        }

        TreeMap<String, Integer> patternCount = new TreeMap<String, Integer>();
        TreeMap<String, ArrayList<String>> map = new TreeMap<String, ArrayList<String>>();
        int revealindex = 0;
        String activePattern = "";

        usedletters.add(guess);
        guessesMade = guessesMade + " " + guess;
        guessesTotal++;

        for (int i = 0; i < active.size(); i++) {
            activePattern = "";
            activePattern = makePattern(active.get(i), guess);
            if (patternCount.containsKey(activePattern)){
                patternCount.put(activePattern, patternCount.get(activePattern) + 1);
            } else {
                patternCount.put(activePattern, 1);
            }
        }

        // decides to call methods to get pattern based on difficulty setting
        String maxkey =  pickDifficultyMethod(patternCount);

        // decreases count of guesses
        if (onlyDashes(maxkey) == false) {
            numberGuesses = numberGuesses - 1;
        }

        // combines two strings into one
        activePattern = combineStrings(activePattern, maxkey);

        // remove words that dont follow the pattern
        removeWords(maxkey, guess);

        pattern = activePattern;

        return patternCount;
    }

    // Method to remove words
    private void removeWords(String maxkey, char guess) {
        for(int i = active.size() - 1; i >= 0; i--) {
            String currentPat = makePattern(active.get(i), guess);
            if(!currentPat.equals(maxkey)) {
                active.remove(i);
            }
        }
    }

    // Method to return the hardest difficulty
    private String pickDifficultyMethod(TreeMap<String, Integer> patternCount) { 
        String maxkey = "";
        if (difficulty == 1) {
            if (guessesTotal % 2 == 0){
                maxkey = getSecondHardestDiff(patternCount);
            } else {
                maxkey = getHardestDiff(patternCount);
            }
        } else if (difficulty == 2) {
            if (guessesTotal % 4 == 0){
                maxkey = getSecondHardestDiff(patternCount);
            } else {
                maxkey = getHardestDiff(patternCount);
            }
        } else {
            maxkey = getHardestDiff(patternCount);
        }
        return maxkey;
    }

    // Method to try to find the number of dashes
    private Boolean onlyDashes(String maxkey) {
        for (int i = 0; i < wordLength; i++) {
            if (maxkey.charAt(i) != '-') {
                return true;
            }
        }
        return false;
    }

    // Method to combine the active pattern and previous pattern
    private String combineStrings(String activePattern, String maxkey) {
        activePattern = "";
        for (int i = 0; i < wordLength; i++) {
            if (pattern.charAt(i) == '-') {
                if (maxkey.charAt(i) != '-') {
                    activePattern = activePattern + maxkey.charAt(i);
                } else {
                    activePattern = activePattern + pattern.charAt(i);
                }
            } else {
                activePattern = activePattern + pattern.charAt(i);
            }
        }
        return activePattern;
    }

    // Method to return the string of the hardest difficulty
    private String getHardestDiff(TreeMap<String, Integer> patternCount) {
        String maxkey = "";
        int max = 0;
        ArrayList<String> tiebreaker =  new ArrayList<String>();

        for (String s : patternCount.keySet()){
            if (patternCount.get(s) > max){ 
                max = patternCount.get(s);
                maxkey = s;
                tiebreaker.add(s);
            } else if (patternCount.get(s) == max) {
                tiebreaker.add(s);
            }
        }

        if (tiebreaker.size() >= 1) {
            tiebreaker.add(maxkey);
            max = 0;
            int maxdash = 0;
            for (int i = 0; i < tiebreaker.size(); i++) {
                for (int j = 0; j < wordLength; j++) {
                    if (tiebreaker.get(i).charAt(j) == '-') {
                        maxdash++;
                    }
                }
                if (maxdash > max) {
                    max = maxdash;
                    maxkey = tiebreaker.get(i);
                }
            }
        }

        return maxkey;
    }

    // Method to return the string of the second hardest difficulty
    private String getSecondHardestDiff(TreeMap<String, Integer> patternCount) {
        String maxkey = "";
        int max = 0;
        int ignorekey = 0;
        ArrayList<String> tiebreaker =  new ArrayList<String>();

        for (String s : patternCount.keySet()){
            if (patternCount.get(s) > max){ 
                max = patternCount.get(s);
                maxkey = s;
            }
        }

        ignorekey = max;
        max = 0;

        for (String s : patternCount.keySet()) {
            if (patternCount.get(s) > max && patternCount.get(s) != ignorekey){ 
                max = patternCount.get(s);
                maxkey = s;
                tiebreaker.add(s);
            }
            if (patternCount.get(s) == max) {
                tiebreaker.add(s);
            }
        }

        if (tiebreaker.size() >= 1) {
            tiebreaker.add(maxkey);
            max = 0;
            int maxdash = 0;

            for (int i = 0; i < tiebreaker.size(); i++) {
                for (int j = 0; j < wordLength; j++) {
                    if (tiebreaker.get(i).charAt(j) == '-') {
                        maxdash = maxdash + 1;
                    }
                }
                if (maxdash > max) {
                    max = maxdash;
                    maxkey = tiebreaker.get(i);
                }
            }
        } 

        return maxkey;
    }

    // Method to make and return an active pattern
    private String makePattern(String word, char guess) {
        String activePattern = "";
        int max = 0;

        for (int j = 0; j < wordLength; j++) {
            if (pattern.charAt(j) == '-') {
                if (word.charAt(j) == guess) {
                    activePattern = activePattern + guess;
                } else {
                    activePattern = activePattern + "-";
                }
            } else {
                activePattern = activePattern + pattern.charAt(j);
            }
        }
        return activePattern;
    }

    // Returns the secret word after there is only one pattern 
    /**
     * Return the secret word this HangmanManager finally ended up picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        Random num = new Random();
        String secretword = active.get(num.nextInt(active.size()));
        return secretword;
    }
}