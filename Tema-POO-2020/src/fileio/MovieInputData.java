package fileio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about a movie, retrieved from parsing the input test files
 * <p>
 * DO NOT MODIFY
 */
public final class MovieInputData extends ShowInput {
    /**
     * Duration in minutes of a season
     */
    private final int duration;

    public MovieInputData(final String title, final ArrayList<String> cast,
                          final ArrayList<String> genres, final int year,
                          final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }


    @Override
    public String toString() {
        return "MovieInputData{" + "title= "
                + super.getTitle() + "year= "
                + super.getYear() + "duration= "
                + duration + "cast {"
                + super.getCast() + " }\n"
                + "genres {" + super.getGenres() + " }\n ";
    }

    //My fields
    private HashMap<String, Double> Rating = new HashMap<>();

    public Map<String, Double> getRating() {
        return Rating;
    }

    public void setRating(HashMap<String, Double> rating) {
        Rating = rating;
    }

    private double grade;

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    private ArrayList<Double> arrayRating = new ArrayList<>();

    public ArrayList<Double> getArrayRating() {
        return arrayRating;
    }

    public void setArrayRating(ArrayList<Double> arrayRating) {
        this.arrayRating = arrayRating;
    }
}
