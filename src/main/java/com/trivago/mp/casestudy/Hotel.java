package com.trivago.mp.casestudy;

/**
 * Stores all relevant information about a particular hotel.
 */
public class Hotel {
    private final int id;
    private final String name;
    private final Integer cityId;
    private final int rating;
    private final int stars;

    public Hotel(int id, String name, Integer cityId, int rating, int stars) {
        this.id = id;
        this.name = name;
        this.cityId = cityId;
        this.rating = rating;
        this.stars = stars;
    }

    /**
     * A unique id as specified in the corresponding .csv file
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * The English name of the hotel
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * The id of the city
     *
     * @return
     */
    public Integer getCityId() {
        return cityId;
    }

    /**
     * The ratings given by user feedback, between 0-100
     *
     * @return
     */
    public int getRating() {
        return rating;
    }

    /**
     * The star rating for the given hotel, between 1-5
     *
     * @return
     */
    public int getStars() {
        return stars;
    }

    @Override
    public String toString() {
        return "Hotel{" + "id=" + id + ", name='" + name + '\'' + ", rating=" + rating + ", stars=" + stars + '}';
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hotel other = (Hotel) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
