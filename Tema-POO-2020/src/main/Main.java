package main;

import checker.Checkstyle;
import checker.Checker;
import common.Constants;
import fileio.*;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * Call the main checker and the coding style checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(Constants.TESTS_PATH);
        Path path = Paths.get(Constants.RESULT_PATH);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        File outputDirectory = new File(Constants.RESULT_PATH);

        Checker checker = new Checker();
        checker.deleteFiles(outputDirectory.listFiles());

        for (File file : Objects.requireNonNull(directory.listFiles())) {

            String filepath = Constants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getAbsolutePath(), filepath);
            }
        }

        checker.iterateFiles(Constants.RESULT_PATH, Constants.REF_PATH, Constants.TESTS_PATH);
        Checkstyle test = new Checkstyle();
        test.testCheckstyle();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     * @return
     */
    public static void action(final String filePath1,
                             final String filePath2) throws IOException {
        InputLoader inputLoader = new InputLoader(filePath1);
        Input input = inputLoader.readData();

        Writer fileWriter = new Writer(filePath2);
        JSONArray arrayResult = new JSONArray();

        List<UserInputData> users = input.getUsers();
        List<MovieInputData> movies = input.getMovies();
        List<SerialInputData> serials = input.getSerials();
        List<ActionInputData> actions = input.getCommands();
        List<ActorInputData> actors = input.getActors();
        ArrayList<String> best_N_actors = new ArrayList<>();

        String message = null;
        int seen = 0;

        for (ActionInputData action : actions) {

            //  ---> COMMANDS <---  //

            if (action.getActionType().equals("command")) {

                //For view command
                if (action.getType().equals("view")) {
                    //System.out.println(actions.get(i));
                    String username = action.getUsername();
                    String title = action.getTitle();

                    for (UserInputData user : users) {

                        if (user.getUsername().equals(username)) {

                            //Doesn't exist
                            if (!user.getHistory().containsKey(title)) {
                                user.getHistory().put(title, 1);
                                message = "success -> " + title +
                                        " was viewed with total views of 1";

                                //Does exist
                            } else {
                                int view = user.getHistory().get(title);
                                view++;
                                user.getHistory().replace(title, view);
                                message = "success -> " + title +
                                        " was viewed with total views of "
                                        + view;
                            }

                        }

                    }

                }

                //For favorite command
                if (action.getType().equals("favorite")) {
                    String username = action.getUsername();
                    String title = action.getTitle();

                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {

                            //Seen
                            if(user.getHistory().containsKey(title)){

                                //Doesn't contain it already
                                if(!user.getFavoriteMovies().contains(title)){
                                    user.getFavoriteMovies().add(title);
                                    message = "success -> " + title +
                                            " was added as favourite";

                                //Already contains it
                                }else{
                                    message = "error -> " + title +
                                            " is already" +
                                            " in favourite list";
                                }
                            //Not seen
                            }else{
                                message = "error -> " + title + " is not seen";
                            }
                        }
                    }
                }

                //For rating command
                if (action.getType().equals("rating")) {
                    String title = action.getTitle();                           //Title serial
                    String username = action.getUsername();                     //Username

                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            if (user.getHistory().containsKey(title)) {         //Check if the show is seen
                                seen = 1;
                            }
                        }
                    }
                    if(seen != 0){
                        if(action.getSeasonNumber() != 0){                      //Daca nr. sez. != 0 => SERIAL
                            for (SerialInputData serial : serials) {
                                if (serial.getTitle().equals(title)) {

                                    int idx = action.getSeasonNumber() - 1;
                                    double rating = action.getGrade();

                                    serial.getSeasons().get(idx).getRating().
                                            put(username, rating);

                                    serial.getSeasons().get(idx).getGrades().
                                            add(rating);

                                    message = "success -> " + title +
                                            " was rated with " + rating +
                                            " by " + username;
                                }
                            }
                        }else{                                                  //Daca nr. sez = 0 => MOVIE
                            for (MovieInputData movie : movies) {
                                if (movie.getTitle().equals(title)) {
                                    double rating = action.getGrade();
                                    movie.getRating().put(username, rating);

                                    movie.getArrayRating().add(rating);

                                    message = "success -> " + title +
                                            " was rated with " + rating +
                                            " by " + username;
                                }
                            }
                        }
                    }else{
                        message = "error -> " + title + " is not seen";
                    }
                    seen = 0;
                }
            }

            //  --->  QUERIES  <---  //

                //Average -> primii N actori in functie de media ratingurilor

            if(action.getActionType().equals("query")){
                int N = action.getNumber();

                //Calculate grade for each movie
                for (MovieInputData movie : movies) {
                    double S = Double.valueOf(0);
                    int nr = 0;
                    double grade;

                    for (int k = 0; k < movie.getArrayRating().size(); k++) {
                        S = S + movie.getArrayRating().get(k);
                        nr++;

                    }
                    grade = S/nr;
                    movie.setGrade(grade);
                }

                //Calculate grade for each serial
                for (SerialInputData serial : serials) {
                    double S = Double.valueOf(0);
                    int nr = 0;
                    double grade;

                    for (int k = 0; k < serial.getSeasons().size(); k++) {         //how many seasons
                        for(int j = 0; j < serial.getSeasons().get(k).
                                getGrades().size(); j++){                          //how many grades for each season
                            S = S + serial.getSeasons().get(k).getGrades().
                                    get(j);
                            nr++;
                        }
                    }

                    grade = S/nr;
                    serial.setGrade(grade);
                }

                //Calculate for each actor a grade
                for (ActorInputData actor : actors) {
                    double S = Double.valueOf(0);
                    int nr = 0;
                    double grade;

                    for (MovieInputData movie : movies) {
                        for (int j = 0; j < movie.getCast().size(); j++) {
                            if(movie.getCast().get(j).equals(actor.getName())){
                                S = S + movie.getGrade();
                                nr++;
                            }
                        }
                    }

                    for (SerialInputData serial : serials) {
                        for (int k = 0; k < serial.getCast().size(); k++) {
                            if(serial.getCast().get(k).equals(actor.getName())){
                                S = S + serial.getGrade();
                                nr++;
                            }
                        }
                    }

                    grade = S/nr;
                    actor.setGrade(grade);
                }

                Collections.sort(actors, Comparator.comparing(ActorInputData::getGrade)
                        .thenComparing(ActorInputData::getName));
//
//                for (ActorInputData actor : actors) {
//                    if (actor.getGrade() < 0) {
//                        System.out.println(actor.getGrade());
//                    }
//                }

                if(action.getSortType().equals("asc")){
                    int index = 0;
                    if(actors.size() != 0){
                        while(index < N){
                            best_N_actors.add(actors.get(index).getName());
                            index++;
                        }
                    }
                    message = "Query result: " + best_N_actors;
                }
                if(action.getSortType().equals("desc")){
                    int index = N - 1;
                    if(actors.size() != 0){
                        while(index >= 0){
                            best_N_actors.add(actors.get(index).getName());
                            index--;
                        }
                    }
                    message = "Query result: " + best_N_actors;
                }








            }

            if (message != null) {
                arrayResult.add(fileWriter.writeFile(action.
                        getActionId(), message));
            }
            message = null;
        }

        //TODO add here the entry point to your implementation

        //System.out.println("ARRAY" + arrayResult);
        fileWriter.closeJSON(arrayResult);
    }
}
