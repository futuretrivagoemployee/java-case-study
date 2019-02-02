package com.trivago.mp.casestudy;

/**
 * Stores the id and name of an advertiser. An Advertiser is a company provides offers for hotel stays.
 */
public class Advertiser {
    private final int id;
    private final String name;

    public Advertiser(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * A unique id as specified in the corresponding .csv file
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Advertiser{" + "id=" + id + ", name='" + name + '\'' + '}';
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
		Advertiser other = (Advertiser) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
