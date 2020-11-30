package main;

import actor.ActorsAwards;
import checker.Checker;
import checker.Checkstyle;
import common.Constants;
import fileio.*;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
     *
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

        checker.iterateFiles(Constants.RESULT_PATH, Constants.REF_PATH,
                Constants.TESTS_PATH);
        Checkstyle test = new Checkstyle();
        test.testCheckstyle();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @return
     * @throws IOException in case of exceptions to reading / writing
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
        ArrayList<String> bestNactors = new ArrayList<>();
        List<ActorInputData> copyactors = new ArrayList<>();
        ArrayList<ShowInput> shows;
        ArrayList<String> showrating = new ArrayList<>();
        ArrayList<String> showfavorite, showlongest, showcopy, usercopy,
                usernumrating;
        ArrayList<String> showviews = new ArrayList<>();
        HashMap<String, Integer> genres;

        String message = null;
        int seen = 0, foundYear, foundGenre, idx, N, nr, index, numRatings,
                ok, view, numberAwards, number, duration;
        double S, grade, rating;

        for (ActionInputData action : actions) {

            /**
             *          ---> COMMANDS <---
             */

            if (action.getActionType().equals("command")) {

                /**
                 *           *VIEW*
                 */

                if (action.getType().equals("view")) {
                    String username = action.getUsername();
                    String title = action.getTitle();

                    for (UserInputData user : users) {

                        if (user.getUsername().equals(username)) {

                            if (!user.getHistory().containsKey(title)) {
                                user.getHistory().put(title, 1);
                                message = "success -> " + title
                                        + " was viewed with total views of 1";

                            } else {
                                view = user.getHistory().get(title);
                                view++;
                                user.getHistory().replace(title, view);
                                message = "success -> " + title
                                        + " was viewed with total views of "
                                        + view;
                            }

                        }

                    }

                }

                /**
                 *             *FAVORITE*
                 */

                if (action.getType().equals("favorite")) {
                    String username = action.getUsername();
                    String title = action.getTitle();

                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {

                            if (user.getHistory().containsKey(title)) {

                                if (!user.getFavoriteMovies().contains(title)) {
                                    user.getFavoriteMovies().add(title);
                                    message = "success -> " + title
                                            + " was added as favourite";

                                } else {
                                    message = "error -> " + title
                                            + " is already"
                                            + " in favourite list";
                                }

                            } else {
                                message = "error -> " + title + " is not seen";
                            }
                        }
                    }
                }

                /**
                 *                *RATING*
                 */

                if (action.getType().equals("rating")) {
                    String title = action.getTitle();
                    String username = action.getUsername();

                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            if (user.getHistory().containsKey(title)) {
                                seen = 1;
                            }
                        }
                    }
                    if (seen != 0) {
                        if (action.getSeasonNumber() != 0) {
                            for (SerialInputData serial : serials) {
                                if (serial.getTitle().equals(title)) {

                                    idx = action.getSeasonNumber() - 1;
                                    rating = action.getGrade();

                                    if (serial.getSeasons().get(idx).getRating()
                                            .containsKey(username)) {
                                        message = "error -> " + title
                                                + " has been already rated";
                                    } else {
                                        serial.getSeasons().get(idx).getRating()
                                                .put(username, rating);

                                        serial.getSeasons().get(idx).getGrades()
                                                .add(rating);

                                        message = "success -> " + title
                                                + " was rated with " + rating
                                                + " by " + username;
                                    }
                                }
                            }
                        } else {
                            for (MovieInputData movie : movies) {
                                if (movie.getTitle().equals(title)) {
                                    rating = action.getGrade();

                                    if (movie.getRating()
                                            .containsKey(username)) {
                                        message = "error -> " + title
                                                + " has been already rated";
                                    } else {
                                        movie.getRating().put(username, rating);

                                        movie.getArrayRating().add(rating);

                                        message = "success -> " + title
                                                + " was rated with " + rating
                                                + " by " + username;
                                    }
                                }
                            }
                        }
                    } else {
                        message = "error -> " + title + " is not seen";
                    }
                    seen = 0;
                }
            }

            /**
             *          ---> QUERIES <---
             */


            if (action.getActionType().equals("query")) {
                N = action.getNumber();

                for (MovieInputData movie : movies) {
                    S = 0;
                    nr = 0;

                    for (int k = 0; k < movie.getArrayRating().size(); k++) {
                        if (movie.getArrayRating().get(k) > 0) {
                            S = S + movie.getArrayRating().get(k);
                            nr++;
                        }
                    }
                    grade = S / nr;
                    movie.setGrade(grade);
                }

                for (SerialInputData serial : serials) {
                    S = 0;
                    nr = 0;

                    for (int k = 0; k < serial.getSeasons().size(); k++) {
                        for (int j = 0; j < serial.getSeasons().get(k)
                                .getGrades().size(); j++) {
                            if (serial.getSeasons().get(k).getGrades()
                                    .get(j) > 0) {
                                S = S + serial.getSeasons().get(k).getGrades()
                                        .get(j);
                                nr++;
                            }
                        }
                    }
                    grade = S / nr;
                    serial.setGrade(grade);
                }

                for (ActorInputData actor : actors) {
                    S = 0;
                    nr = 0;

                    for (MovieInputData movie : movies) {
                        for (int j = 0; j < movie.getCast().size(); j++) {
                            if (movie.getCast().get(j)
                                    .equals(actor.getName())) {
                                if (movie.getGrade() > 0) {
                                    S = S + movie.getGrade();
                                    nr++;
                                }
                            }
                        }
                    }
                    for (SerialInputData serial : serials) {
                        for (int k = 0; k < serial.getCast().size(); k++) {
                            if (serial.getCast().get(k)
                                    .equals(actor.getName())) {
                                if (serial.getGrade() > 0) {
                                    S = S + serial.getGrade();
                                    nr++;
                                }
                            }
                        }
                    }
                    grade = S / nr;
                    actor.setGrade(grade);
                }


                /**
                 *             *AVERAGE*
                 */

                if (action.getCriteria().equals("average")) {

                    copyactors = new ArrayList<>();
                    for (ActorInputData actor : actors) {
                        if (actor.getGrade() > 0) {
                            copyactors.add(actor);
                        }
                    }

                    copyactors.sort(Comparator
                            .comparing(ActorInputData::getGrade)
                            .thenComparing(ActorInputData::getName));


                    if (action.getSortType().equals("asc")) {
                        index = 0;
                        if ((copyactors.size() != 0)
                                && (copyactors.size() > N)) {
                            while (index < N) {
                                bestNactors
                                        .add(copyactors.get(index).getName());
                                index++;
                            }
                        }
                        if (copyactors.size() <= N) {
                            while (index < copyactors.size()) {
                                bestNactors
                                        .add(copyactors.get(index).getName());
                                index++;
                            }
                        }
                        message = "Query result: " + bestNactors;
                    }

                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((copyactors.size() != 0)
                                && (copyactors.size() > N)) {
                            while (index >= 0) {
                                bestNactors
                                        .add(copyactors.get(index).getName());
                                index--;
                            }
                        }
                        if (copyactors.size() >= N) {
                            index = copyactors.size() - 1;
                            while (index >= 0) {
                                bestNactors
                                        .add(copyactors.get(index).getName());
                                index--;
                            }
                        }
                        message = "Query result: " + bestNactors;
                    }

                }

                /**
                 *              *AWARDS*
                 */

                if (action.getCriteria().equals("awards")) {

                    for (ActorInputData actor : actors) {
                        numberAwards = 0;
                        for (ActorsAwards key : actor.getAwards().keySet()) {
                            numberAwards += actor.getAwards().get(key);
                        }
                        actor.setNumberAwards(numberAwards);
                    }


                    for (ActorInputData actor : actors) {
                        if (actor.getAwards().size() != 0) {
                            copyactors.add(actor);
                        }
                    }
                }

                if (action.getCriteria().equals("ratings")) {
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
                        index = 0;
                        if ((shows.size() != 0) && (shows.size() > N)) {
                            while (index < N) {
                                showrating.add(shows.get(index).getTitle());
                                index++;
                            }
                        }
                        message = "Query result: " + showrating;
                    }
                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((shows.size() != 0) && (shows.size() > N)) {
                            while (index >= 0) {
                                showrating.add(shows.get(index).getTitle());
                                index--;
                            }
                        }
                        message = "Query result: " + showrating;
                    }
                }

                /**
                 *              *FAVORITE*
                 */

                if (action.getCriteria().equals("favorite")) {
                    shows = new ArrayList<>();

                    shows.addAll(movies);
                    shows.addAll(serials);

                    for (ShowInput show : shows) {
                        number = 0;
                        for (UserInputData user : users) {
                            for (int k = 0; k < user.getFavoriteMovies().size();
                                 k++) {
                                if (show.getTitle().equals(user.
                                        getFavoriteMovies().get(k))) {
                                    number++;
                                }
                            }
                        }
                        show.setAppearances(number);
                    }

                    shows.sort(Comparator.comparing(ShowInput::getAppearances));
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);

                    showcopy = new ArrayList<>();
                    for (ShowInput show : shows) {
                        foundYear = 0;
                        foundGenre = 0;
                        if (year == null) {
                            foundYear = 1;
                        } else if (show.getYear() == Integer.parseInt(year)) {
                            foundYear = 1;
                        }
                        for (int i = 0; i < show.getGenres().size(); i++) {
                            if (genre == null) {
                                foundGenre = 1;
                            } else if (show.getGenres().get(i).equals(genre)) {
                                foundGenre = 1;
                                break;
                            }
                        }
                        if (show.getAppearances() == 0) {
                            foundGenre = 0;
                        }
                        if ((foundYear == 1) && (foundGenre == 1)) {
                            showcopy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    showfavorite = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        index = 0;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index < N) {
                                showfavorite.add(showcopy.get(index));
                                index++;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = 0;
                            while (index < showcopy.size()) {
                                showfavorite.add(showcopy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + showfavorite;
                    }
                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index >= 0) {
                                showfavorite.add(showcopy.get(index));
                                index--;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = showcopy.size() - 1;
                            while (index >= 0) {
                                showfavorite.add(showcopy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + showfavorite;
                    }


                }

                /**
                 *               *LONGEST*
                 */

                if (action.getCriteria().equals("longest")) {

                    for (SerialInputData serial : serials) {
                        duration = 0;
                        for (int j = 0; j < serial.getSeasons().size(); j++) {
                            duration += serial.getSeasons().get(j)
                                    .getDuration();
                        }
                        serial.setDuration(duration);
                    }

                    shows = new ArrayList<>();
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);
                    shows.addAll(movies);
                    shows.addAll(serials);
                    shows.sort(Comparator.comparing(ShowInput::getDuration));

                    showcopy = new ArrayList<>();
                    for (ShowInput show : shows) {
                        foundYear = 0;
                        foundGenre = 0;
                        if (year == null) {
                            foundYear = 1;
                        } else if (show.getYear() == Integer.parseInt(year)) {
                            foundYear = 1;
                        }
                        for (int i = 0; i < show.getGenres().size(); i++) {
                            if (show.getGenres().get(i).equals(genre)) {
                                foundGenre = 1;
                                break;
                            }
                        }
                        if ((foundYear == 1) && (foundGenre == 1)) {
                            showcopy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    showlongest = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        index = 0;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index < N) {
                                showlongest.add(showcopy.get(index));
                                index++;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = 0;
                            while (index < showcopy.size()) {
                                showlongest.add(showcopy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + showlongest;
                    }
                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index >= 0) {
                                showlongest.add(showcopy.get(index));
                                index--;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = showcopy.size() - 1;
                            while (index > showcopy.size()) {
                                showlongest.add(showcopy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + showlongest;
                    }

                }

                /**
                 *               *MOST VIEWED*
                 */

                if (action.getCriteria().equals("most_viewed")) {

                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    for (ShowInput show : shows) {
                        view = 0;
                        for (UserInputData user : users) {
                            if (user.getHistory().containsKey(show.getTitle())) {
                                view += user.getHistory().get(show.getTitle());
                            }
                        }
                        show.setView(view);
                    }

                    shows.sort(Comparator.comparing(ShowInput::getView));

                    showcopy = new ArrayList<>();
                    String year = action.getFilters().get(0).get(0);
                    String genre = action.getFilters().get(1).get(0);
                    for (ShowInput show : shows) {
                        foundYear = 0;
                        foundGenre = 0;
                        if (year == null) {
                            foundYear = 1;
                        } else if (show.getYear() == Integer.parseInt(year)) {
                            foundYear = 1;
                        }
                        for (int i = 0; i < show.getGenres().size(); i++) {
                            if (show.getGenres().get(i).equals(genre)) {
                                foundGenre = 1;
                                break;
                            }
                        }
                        if (show.getView() == 0) {
                            foundGenre = 0;
                        }
                        if ((foundYear == 1) && (foundGenre == 1)) {
                            showcopy.add(show.getTitle());
                        }
                    }

                    N = action.getNumber();
                    if (action.getSortType().equals("asc")) {
                        index = 0;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index < N) {
                                showviews.add(showcopy.get(index));
                                index++;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = 0;
                            while (index < showcopy.size()) {
                                showviews.add(showcopy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + showviews;
                    }
                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((showcopy.size() != 0) && (showcopy.size() > N)) {
                            while (index >= 0) {
                                showviews.add(showcopy.get(index));
                                index--;
                            }
                        }
                        if (showcopy.size() <= N) {
                            index = showcopy.size() - 1;
                            while (index >= 0) {
                                showviews.add(showcopy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + showviews;
                    }

                }

                /**
                 *               *NUMBER OF RATINGS*
                 */

                if (action.getCriteria().equals("num_ratings")) {

                    for (UserInputData user : users) {
                        numRatings = 0;
                        for (SerialInputData serial : serials) {
                            for (int j = 0; j < serial.getSeasons()
                                    .size(); j++) {
                                if (serial.getSeasons().get(j).getRating().
                                        containsKey(user.getUsername())) {
                                    numRatings++;
                                }
                            }
                        }
                        for (MovieInputData movie : movies) {
                            if (movie.getRating()
                                    .containsKey(user.getUsername())) {
                                numRatings++;
                            }
                        }
                        user.setNumRatings(numRatings);
                    }

                    users.sort(Comparator
                            .comparing(UserInputData::getNumRatings));

                    usercopy = new ArrayList<>();
                    for (UserInputData user : users) {
                        if (user.getNumRatings() > 0) {
                            usercopy.add(user.getUsername());
                        }
                    }

                    N = action.getNumber();
                    usernumrating = new ArrayList<>();
                    if (action.getSortType().equals("asc")) {
                        index = 0;
                        if ((usercopy.size() != 0) && (usercopy.size() > N)) {
                            while (index < N) {
                                usernumrating.add(usercopy.get(index));
                                index++;
                            }
                        }
                        if (usercopy.size() <= N) {
                            index = 0;
                            while (index < usercopy.size()) {
                                usernumrating.add(usercopy.get(index));
                                index++;
                            }
                        }
                        message = "Query result: " + usernumrating;
                    }
                    if (action.getSortType().equals("desc")) {
                        index = N - 1;
                        if ((usercopy.size() != 0) && (usercopy.size() > N)) {
                            while (index >= 0) {
                                usernumrating.add(usercopy.get(index));
                                index--;
                            }
                        }
                        if (usercopy.size() <= N) {
                            index = usercopy.size() - 1;
                            while (index >= 0) {
                                usernumrating.add(usercopy.get(index));
                                index--;
                            }
                        }
                        message = "Query result: " + usernumrating;
                    }
                }


            }

            /**
             *          ---> RECOMMENDATION <---
             */

            if (action.getActionType().equals("recommendation")) {

                /**
                 *               *STANDARD*
                 */

                if (action.getType().equals("standard")) {
                    String username = action.getUsername();
                    UserInputData thatUser;
                    thatUser = null;
                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            thatUser = user;
                        }
                    }

                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    for (ShowInput show : shows) {
                        ok = 0;
                        assert thatUser != null;
                        if (thatUser.getHistory()
                                .containsKey(show.getTitle())) {
                            ok = 1;
                        }
                        if (ok == 0) {
                            message = "StandardRecommendation result: "
                                    + show.getTitle();
                            break;
                        }
                    }
                }

                /**
                 *               *BEST UNSEEN*
                 */

                if (action.getType().equals("best_unseen")) {
                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);
                    shows.sort(Comparator.comparing(ShowInput::getGrade));

                    String username = action.getUsername();
                    UserInputData thatUser = null;
                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            thatUser = user;
                        }
                    }

                    for (ShowInput show : shows) {
                        ok = 0;
                        assert thatUser != null;
                        if (thatUser.getHistory()
                                .containsKey(show.getTitle())) {
                            ok = 1;
                        }

                        if (ok == 0) {
                            message = "BestRatedUnseenRecommendation result: "
                                    + show.getTitle();
                            break;
                        }

                    }

                }

                /**
                 *                    *POPULAR*
                 */

                if (action.getType().equals("popular")) {

                    int views;
                    genres = new HashMap<>();
                    shows = new ArrayList<>();
                    shows.addAll(movies);
                    shows.addAll(serials);

                    for (ShowInput show : shows) {
                        view = 0;
                        for (UserInputData user : users) {
                            if (user.getHistory().containsKey(show.getTitle())) {
                                view += user.getHistory().get(show.getTitle());
                            }
                        }
                        show.setView(view);
                    }

                    for (ShowInput show : shows) {
                        for (int j = 0; j < show.getGenres().size(); j++) {
                            if (genres.containsKey(show.getGenres().get(j))) {
                                views = genres.get(show.getGenres().get(j));
                                views += show.getView();
                                genres.put(show.getGenres().get(j), views);
                            } else {
                                views = show.getView();
                                genres.put(show.getGenres().get(j), views);
                            }
                        }
                    }

                    Map<String, Integer> sorted = genres.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new));

                    String username = action.getUsername();
                    UserInputData thisUser = null;
                    for (UserInputData user : users) {
                        if (user.getUsername().equals(username)) {
                            if (user.getSubscriptionType().equals("PREMIUM")) {
                                thisUser = user;
                            }
                        }
                    }

                    int found = 0;
                    if (thisUser != null) {
                        for (String key : sorted.keySet()) {
                            for (ShowInput show : shows) {
                                if (show.getGenres().contains(key)) {
                                    if (thisUser.getHistory().containsKey(show.getTitle())) {
                                        found = 0;
                                    } else {
                                        found = 1;
                                        message = "PopularRecommendation result: " + show.getTitle();
                                    }
                                    if (found == 1) {
                                        break;
                                    }
                                }
                            }
                            if (found == 1) {
                                break;
                            }
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

        fileWriter.closeJSON(arrayResult);
    }
}
