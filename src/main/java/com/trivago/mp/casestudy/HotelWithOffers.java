package com.trivago.mp.casestudy;

import java.util.List;

/**
 * Wraps a hotel and a list of {@link Offer Offers}.
 */
public class HotelWithOffers {
    private final Hotel hotel;

    /**
     * A list of concrete advertiser offers for this hotel
     */
    List<Offer> offers;

    public HotelWithOffers(Hotel hotel) {
        this.hotel = hotel;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    @Override
    public String toString() {
        return "HotelWithOffers{" + "hotel=" + hotel + ", offers=" + offers + '}';
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hotel == null) ? 0 : hotel.hashCode());
		result = prime * result + ((offers == null) ? 0 : offers.hashCode());
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
		HotelWithOffers other = (HotelWithOffers) obj;
		if (hotel == null) {
			if (other.hotel != null)
				return false;
		} else if (!hotel.equals(other.hotel))
			return false;
		if (offers == null) {
			if (other.offers != null)
				return false;
		} else if (!offers.containsAll(other.offers))
			return false;
		return true;
	}
    
    
}
