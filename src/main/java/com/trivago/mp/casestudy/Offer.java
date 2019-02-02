package com.trivago.mp.casestudy;

/**
 * A concrete offer from a specific advertiser
 */
public class Offer {
    private final Advertiser advertiser;
    private final int priceInEuro;
    private final int cpc;

    public Offer(Advertiser advertiser, int priceInEuro, int cpc) {
        this.advertiser = advertiser;
        this.priceInEuro = priceInEuro;
        this.cpc = cpc;
    }

    public Advertiser getAdvertiser() {
        return advertiser;
    }

    public int getPriceInEuro() {
        return priceInEuro;
    }

    /**
     * the cost per click an advertiser pays for a particular offer
     */
    public int getCpc() {
        return cpc;
    }

    @Override
    public String toString() {
        return "Offer{" + "advertiser=" + advertiser + ", priceInEuro=" + priceInEuro + ", cpc=" + cpc + '}';
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((advertiser == null) ? 0 : advertiser.hashCode());
		result = prime * result + cpc;
		result = prime * result + priceInEuro;
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
		Offer other = (Offer) obj;
		if (advertiser == null) {
			if (other.advertiser != null)
				return false;
		} else if (!advertiser.equals(other.advertiser))
			return false;
		if (cpc != other.cpc)
			return false;
		if (priceInEuro != other.priceInEuro)
			return false;
		return true;
	}
}
