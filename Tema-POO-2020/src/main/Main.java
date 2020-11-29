package main;

import actor.ActorsAwards;
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
        List<ActorInputData> copy_actors = new ArrayList<>();
        ArrayList<ShowInput> shows;
        ArrayList<String> show_rating = new ArrayList<>();
        ArrayList<String> show_favorite;
        ArrayList<String> show_longest;
        ArrayList<String> show_copy;
        ArrayList<String> show_views;
        ArrayList<String> user_copy;
        ArrayList<String> user_num_rating;

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

                                    if(serial.getSeasons().get(idx).getRating()
                                            .containsKey(username)){
                                        message = "error -> " + title +
                                                " has been already rated";
                                    }else{
                                        serial.getSeasons().get(idx).getRating()
                                                .put(username, rating);

                                        serial.getSeasons().get(idx).getGrades()
                                                .add(rating);

                                        message = "success -> " + title +
                                                " was rated with " + rating +
                                                " by " + username;
                                    }
                                }
                            }
                        }else{                                                  //Daca nr. sez = 0 => MOVIE
                            for (MovieInputData movie : movies) {
                                if (movie.getTitle().equals(title)) {
                                    double rating = action.getGrade();

                                    if(movie.getRating().containsKey(username)){
                                        message = "error -> " + title +
                                                " has been already rated";
                                    }else{
                                        movie.getRating().put(username, rating);

                                        movie.getArrayRating().add(rating);

                                        message = "success -> " + title +
                                                " was rated with " + rating +
                                                " by " + username;
                                    }
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


            if(action.getActionType().equals("query")) {
                int N = action.getNumber();

                //Calculate grade for each movie
                for (MovieInputData movie : movies) {
                    double S = 0;
                    int nr = 0;
                    double grade;

                    for (int k = 0; k < movie.getArrayRating().size(); k++) {
                        if(movie.getArrayRating().get(k) > 0){
                            S = S + movie.getArrayRating().get(k);
                            nr++;
                        }
                    }
                    grade = S / nr;
                    movie.setGrade(grade);
                }

                //Calculate grade for each serial
                for (SerialInputData serial : serials) {
                    double S = 0;
                    int nr = 0;
                    double grade;

                   // System.out.println(serial.getSeasons());
                    for (int k = 0; k < serial.getSeasons().size(); k++) {
                        for (int j = 0; j < serial.getSeasons().get(k).
                                getGrades().size(); j++) {
                            if(serial.getSeasons().get(k).getGrades().get(j)>0){
                                S = S + serial.getSeasons().get(k).getGrades().
                                        get(j);
                                nr++;
                            }
                        }
                    }
                    grade = S / nr;
                    serial.setGrade(grade);
                }

                //Calculate for each actor a grade
                for (ActorInputData actor : actors) {
                    double S = 0;
                    int nr = 0;
                    double grade;

                    for (MovieInputData movie : movies) {
                        for (int j = 0; j < movie.getCast().size(); j++) {
                            if (movie.getCast().get(j).equals(actor.getName())){
                                if(movie.getGrade() > 0){
                                    S = S + movie.getGrade();
                                    nr++;
                                }
                            }
                        }
                    }
                    for (SerialInputData serial : serials) {
                        for (int k = 0; k < serial.getCast().size(); k++) {
                            if (serial.getCast().get(k).equals(actor.getName())){
                                if(serial.getGrade() > 0){
                                    S = S + serial.getGrade();
                                    nr++;
                                }
                            }
                        }
                    }
                    grade = S / nr;
                    actor.setGrade(grade);
                }


                //Average -> first N actors by ratings
                if(action.getCriteria().equals("average")){

                    copy_actors = new ArrayList<>();
                    for (ActorInputData actor : actors) {
                        if (actor.getGrade() > 0) {
                            copy_actors.add(actor);
                        }
                    }

                    copy_actors.sort(Comparator.comparing(ActorInputData::getGrade)
                            .thenComparing(ActorInputData::getName));

//                    for(int i = 0; i < copy_actors.size(); i++){
//                        System.out.println(copy_actors.get(i).getGrade());
//                    }

                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((copy_actors.size() != 0) && (copy_actors.size() > N)) {
                            while (index < N) {
                                best_N_actors.add(copy_actors.get(index).getName());
                                index++;
                            }
                        }
                        if(copy_actors.size() <= N){
                            while (index < copy_actors.size()) {
                                best_N_actors.add(copy_actors.get(index).getName());
                                index++;
                            }
                        }
                        message = "Query result: " + best_N_actors;
                    }

                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((copy_actors.size() != 0) && (copy_actors.size() > N)) {
                            while (index >= 0) {
                                best_N_actors.add(copy_actors.get(index).getName());
                                index--;
                            }
                        }
                        if(copy_actors.size() >= N){
                            index = copy_actors.size() - 1;
                            while (index >= 0) {
                                best_N_actors.add(copy_actors.get(index).getName());
                                index--;
                            }
                        }
                        message = "Query result: " + best_N_actors;
                    }

                }

                //Awards -> actors with mentioned awards

                if(action.getCriteria().equals("awards")){

                    //Calculate each actor total number of awards
                    for (ActorInputData actor : actors) {
                        int number_awards = 0;
                        for (ActorsAwards key : actor.getAwards().keySet()) {
                            number_awards += actor.getAwards().get(key);
                        }
                        actor.setNumber_awards(number_awards);
                    }


                    for (ActorInputData actor : actors) {
                        if (actor.getAwards().size() != 0) {
                            copy_actors.add(actor);
                        }
                    }
                }


                //Rating -> first N shows by ratings
                if(action.getCriteria().equals("ratings")){
                    shows = new ArrayList<>();
                    for (MovieInputData movie : movies) {
                        if (movie.getGrade() > 0) {
                            shows.add(movie);
                        }
                    }

                    for (SerialInputData serial : serials) {
                        if (serial.getGrade() > 0) {
                            shows.add(serial);
                        }
                    }

                    shows.sort(Comparator.comparing(ShowInput::getGrade));


                    N = action.getNumber();
                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((shows.size() != 0) && (shows.size() > N)) {
                            while (index < N) {
                                show_rating.add(shows.get(index).getTitle());
                                index++;
                            }
                        }
                        message = "Query result: " + show_rating;
                    }
                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((shows.size() != 0) && (shows.size() > N)) {
                            while (index >= 0) {
                                show_rating.add(shows.get(index).getTitle());
                                index--;
                            }
                        }
                        message = "Query result: " + show_rating;
                    }
                }

                //Favorite -> first N shows by appearances
                if(action.getCriteria().equals("favorite")){
                    shows = new ArrayList<>();

                    //Shows contains all movies and serials
                    shows.addAll(movies);
                    shows.addAll(serials);

                    for (ShowInput show : shows) {
                        int number = 0;
                        for (UserInputData user : users) {
                            for (int k = 0; k < user.getFavoriteMovies().size();
                                 k++) {
                                if (show.getTitle().equals(user.
                                        getFavoriteMovies().get(k))){
                                    number++;
                                }
                            }
                        }
                        show.setAppearances(number);
                    }

                    shows.sort(Comparator.comparing(ShowInput::getAppearances));
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);

                    show_copy = new ArrayList<>();
                    for (ShowInput show : shows) {
                        int found_year = 0;
                        int found_genre = 0;
                        if(year == null){
                            found_year = 1;
                        }else if (show.getYear() == Integer.parseInt(year)) {
                            found_year = 1;
                        }
                        for(int i = 0; i < show.getGenres().size(); i++){
                            if(genre == null){
                                found_genre = 1;
                            }else if(show.getGenres().get(i).equals(genre)){
                                found_genre = 1;
                                break;
                            }
                        }
                        if(show.getAppearances() == 0){
                            found_genre =0;
                        }
                        if((found_year == 1) && (found_genre == 1)){
                            show_copy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    show_favorite = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index < N) {
                                show_favorite.add(show_copy.get(index));
                                index++;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = 0;
                           // System.out.println(show_copy);
                            while(index < show_copy.size()){
                                show_favorite.add(show_copy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + show_favorite;
                    }
                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index >= 0) {
                                show_favorite.add(show_copy.get(index));
                                index--;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = show_copy.size() - 1;
                           // System.out.println(index);
                            while(index >= 0){
                                show_favorite.add(show_copy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + show_favorite;
                    }


                }

                //Longest -> first N shows by duration
                if(action.getCriteria().equals("longest")){

                    for (SerialInputData serial : serials) {
                        int duration = 0;
                        for (int j = 0; j < serial.getSeasons().size(); j++){
                            duration += serial.getSeasons().get(j).getDuration();
                        }
                        serial.setDuration(duration);
                    }

                    shows = new ArrayList<>();
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);
                    shows.addAll(movies);
                    shows.addAll(serials);
                    shows.sort(Comparator.comparing(ShowInput::getDuration));

                    show_copy = new ArrayList<>();
                    for (ShowInput show : shows) {
                        int found_year = 0;
                        int found_genre = 0;
                        if(year == null){
                            found_year = 1;
                        }else if (show.getYear() == Integer.parseInt(year)) {
                            found_year = 1;
                        }
                        for(int i = 0; i < show.getGenres().size(); i++){
                            if(show.getGenres().get(i).equals(genre)){
                                found_genre = 1;
                                break;
                            }
                        }
                        if((found_year == 1) && (found_genre == 1)){
                            show_copy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    show_longest = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index < N) {
                                show_longest.add(show_copy.get(index));
                                index++;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = 0;
                            while(index < show_copy.size()){
                                show_longest.add(show_copy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + show_longest;
                    }
                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index >= 0) {
                                show_longest.add(show_copy.get(index));
                                index--;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = show_copy.size() - 1;
                            while(index > show_copy.size()){
                                show_longest.add(show_copy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + show_longest;
                    }

                }

                //Most Viewed -> first N by views
                if(action.getCriteria().equals("most_viewed")){

                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    for (ShowInput show : shows) {
                        int view = 0;
                        for (UserInputData user : users) {
                            if (user.getHistory().containsKey(show.getTitle())){
                                view += user.getHistory().get(show.getTitle());
                            }
                        }
                        show.setView(view);
                    }

                    shows.sort(Comparator.comparing(ShowInput::getView));

                    show_copy = new ArrayList<>();
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);
                    for (ShowInput show : shows) {
                        int found_year = 0;
                        int found_genre = 0;
                        if(year == null){
                            found_year = 1;
                        }else if (show.getYear() == Integer.parseInt(year)) {
                            found_year = 1;
                        }
                        for(int i = 0; i < show.getGenres().size(); i++){
                            if(show.getGenres().get(i).equals(genre)){
                                found_genre = 1;
                                break;
                            }
                        }
                        if(show.getView() == 0){
                            found_genre = 0;
                        }
                        if((found_year == 1) && (found_genre == 1)){
                            show_copy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    show_views = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index < N) {
                                show_views.add(show_copy.get(index));
                                index++;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = 0;
                            while(index < show_copy.size()){
                                show_views.add(show_copy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + show_views;
                    }
                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((show_copy.size() != 0) && (show_copy.size() > N)) {
                            while (index >= 0) {
                                show_views.add(show_copy.get(index));
                                index--;
                            }
                        }
                        if(show_copy.size() <= N){
                            index = show_copy.size() - 1;
                            while(index >= 0){
                                show_views.add(show_copy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + show_views;
                    }

                }

                if(action.getCriteria().equals("num_ratings")){

                    for (UserInputData user : users) {
                        int num_ratings = 0;
                        for (SerialInputData serial : serials) {
                            for (int j = 0; j < serial.getSeasons().size(); j++){
                                if (serial.getSeasons().get(j).getRating().
                                        containsKey(user.getUsername())) {
                                    num_ratings++;
                                }
                            }
                        }
                        for (MovieInputData movie : movies) {
                            if (movie.getRating().containsKey(user.getUsername())) {
                                num_ratings++;
                            }
                        }
                        user.setNum_ratings(num_ratings);
                    }

                    users.sort(Comparator.comparing(UserInputData::getNum_ratings));

                    user_copy = new ArrayList<>();
                    for (UserInputData user : users) {
                        if (user.getNum_ratings() > 0) {
                            user_copy.add(user.getUsername());
                        }
                    }

                    N = action.getNumber();
                    user_num_rating = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        int index = 0;
                        if ((user_copy.size() != 0) && (user_copy.size() > N)) {
                            while (index < N) {
                                user_num_rating.add(user_copy.get(index));
                                index++;
                            }
                        }
                        if(user_copy.size() <= N){
                            index = 0;
                            while(index < user_copy.size()){
                                user_num_rating.add(user_copy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + user_num_rating;
                    }
                    if (action.getSortType().equals("desc")) {
                        int index = N - 1;
                        if ((user_copy.size() != 0) && (user_copy.size() > N)) {
                            while (index >= 0) {
                                user_num_rating.add(user_copy.get(index));
                                index--;
                            }
                        }
                        if(user_copy.size() <= N){
                            index = user_copy.size() - 1;
                            while(index >= 0){
                                user_num_rating.add(user_copy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + user_num_rating;
                    }
                }



            }

            if(action.getActionType().equals("recommendation")){
                if(action.getType().equals("standard")){
                    String username = action.getUsername();
                    UserInputData that_user = null;
                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            that_user = user;
                        }
                    }

                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    for (ShowInput show : shows) {
                        int ok = 0;
                        assert that_user != null;
                        if (that_user.getHistory().containsKey(show.getTitle())){
                            ok = 1;
                        }
                        if(ok == 0){
                            message = "StandardRecommendation result: " + show.getTitle();
                            break;
                        }
                    }
                }

                if(action.getType().equals("best_unseen")){
                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    shows.sort(Comparator.comparing(ShowInput::getGrade));

                    String username = action.getUsername();
                    UserInputData that_user = null;
                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            that_user = user;
                        }
                    }

                    for (ShowInput show : shows) {
                        int ok = 0;
                        assert that_user != null;
                        if (that_user.getHistory().containsKey(show.getTitle())){
                            ok = 1;
                        }
                        if(ok == 0){
                            message = "BestRatedUnseenRecommendation result: " + show.getTitle();
                            break;
                        }
                    }


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
