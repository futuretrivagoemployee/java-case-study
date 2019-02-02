package com.trivago.mp.casestudy;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard implementation of the {@code HotelSearchEngine} interface. 
 * Initialized by reading hard-written CSV files.  
 * 
 */
public class HotelSearchEngineImpl implements HotelSearchEngine {

	Logger logger = LoggerFactory.getLogger(HotelSearchEngineImpl.class);
	
	private List<Hotel> hotels; 
	private List<Advertiser> advertisers;
	private Map<Integer, List<Integer>> advertiserIdToHotelIds;
	private Map<Integer, String> cityIdToCityName;
	
    @Override
    public void initialize() {
    	try {
    		hotels = CsvDataLoader.loadHotels();
    		advertisers = CsvDataLoader.loadAdvertisers();
    		advertiserIdToHotelIds = CsvDataLoader.loadAdvertiserIdToHotelIds();
    		cityIdToCityName = CsvDataLoader.loadCityIdToCityName();
    	} catch (IOException e) {
    		logger.error("Error initalizing the HotelSearchEngine.");
    	}
    }

    @Override
    public List<HotelWithOffers> performSearch(String cityName, DateRange dateRange, OfferProvider offerProvider) {
    	/* Quick explanation of the algorithm:
    	 * 1. We retrieve all hotels in the given city
    	 * 2. We retrieve all advertisers who have available offers for the relevant hotel
    	 * 3. We query each advertiser via the OfferProvider & update the list of hotel with offers  
    	 * 4. Done
    	 */
    	
    	List<HotelWithOffers> resultList = new ArrayList<HotelWithOffers>();
    	
    	int cityId = getCityIdByName(cityName);
    	
    	List<Integer> hotelsInCity = getHotelIdsByCityId(cityId);
    	
    	List<Advertiser> advertisersForHotels = getAdvertisersByHotelIds(hotelsInCity);
    	
    	for (Advertiser advertiser: advertisersForHotels) {
    		Map<Integer, Offer> hotelIdToOffer = offerProvider.getOffersFromAdvertiser(advertiser, hotelsInCity, dateRange);
    		
    		// The result list might already contain offers from other advertisers,
    		// we refer to a merger method.
    		mergeOffers(resultList, hotelIdToOffer);
    	}
    	
    	return resultList;
    }
    
    /**
     * This method serves two main purposes:
     * <ol>
     * 	<li> Conversion from the low-level {Id -> Offer} to the {@code HotelWithOffers} 
     *  <li> Handles all the various sanity checks necessary when creating a new offer list
     *   or updating an already existing one etc.
     * </ol>
     * 
     * <b>NB:</b> The update is done on the live {@code hotelList} itself.
     * 
     * @param hotelList The current list of hotels with offers
     * @param hotelIdToOffer A map {Id -> Offer} mapping hotels to offers 
     */
	private void mergeOffers(List<HotelWithOffers> hotelList, Map<Integer, Offer> hotelIdToOffer) {

		for (Entry<Integer, Offer> entry: hotelIdToOffer.entrySet()) {
			Integer hotelId = entry.getKey();
			Offer offer = entry.getValue();
			
			Hotel hotel = null;
			HotelWithOffers hotelWithOffers = null;
			// Look for the beans with the right id.
			for (HotelWithOffers result: hotelList) {
				if (result.getHotel().getId() == hotelId) {
					hotel = result.getHotel();
					hotelWithOffers = result;
					break;
				}
			}
			
			if (hotel == null) {
				// We did not find one, create it. 
				hotel = getHotelById(hotelId);
				hotelWithOffers = new HotelWithOffers(hotel);
				List<Offer> offers = new ArrayList<Offer>();
				offers.add(offer);
				hotelWithOffers.setOffers(offers);
			} else {
				// We found one, update the list
				List<Offer> availableOffers = hotelWithOffers.getOffers();
				availableOffers.add(offer);
			}
			
			hotelList.add(hotelWithOffers);
		}
	}

	/**
	 * Retrieve the hotel with the given id.
	 * 
	 * @param hotelId Id of the hotel
	 * 
	 * @return Hotel with the given id
	 */
	private Hotel getHotelById(Integer hotelId) {
		return hotels.stream()
				.filter(hotel -> hotel.getId() == hotelId)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Retrieves advertisers who have offers for the given hotels.
	 * 
	 * @param hotelsInCity List of hotel ids
	 * 
	 * @return List of relevant advertisers
	 */
	private List<Advertiser> getAdvertisersByHotelIds(List<Integer> hotelsInCity) {

		List<Integer> advertiserIds = new ArrayList<Integer>();
		for (Entry<Integer, List<Integer>> advertiserToHotels: advertiserIdToHotelIds.entrySet()) {
			Integer advertiserId = advertiserToHotels.getKey();
			List<Integer> hotelIds = advertiserToHotels.getValue();
			
			List<Integer> hotelsInCityCopy = new ArrayList<Integer>(hotelsInCity);
			hotelsInCityCopy.retainAll(hotelIds);
			
			if (hotelsInCityCopy.size() > 0) {
				advertiserIds.add(advertiserId);
			}
		}
		
		return advertisers.stream()
				.filter(advertiser -> advertiserIds.contains(advertiser.getId()))
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves hotels for the given city.
	 * 
	 * @param cityId Id of the city
	 * 
	 * @return List of relevant hotels
	 */
	private List<Integer> getHotelIdsByCityId(int cityId) {
		return hotels.stream()
				.filter(hotel -> hotel.getCityId() == cityId)
				.map(Hotel::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieve the id of the city with the given name.
	 * 
	 * @param cityName Name of the city
	 * 
	 * @return City with the given name
	 */
	private Integer getCityIdByName(String cityName) {
		return cityIdToCityName.entrySet().stream()
			    .filter(entry -> entry.getValue().equals(cityName))
			    .map(Map.Entry::getKey)
			    .findFirst()
			    .orElse(-1);
	}
	
	/**
	 * This class is responsible for parsing the CSV files holding the data model.
	 * 
	 * Relies on Apache Commons CSV. 
	 * 
	 */
	private static class CsvDataLoader {
		
		private static final File ADVERTISER_FILE = new File("data/advertisers.csv");
		private static final File CITIES_FILE = new File("data/cities.csv");
		private static final File HOTEL_ADVERTISER_FILE = new File("data/hotel_advertiser.csv");
		private static final File HOTELS_FILE = new File("data/hotels.csv");

		/**
		 * Loads the hotels.
		 * 
		 * @return List of hotels
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		static public List<Hotel> loadHotels() throws FileNotFoundException, IOException {
			List<Hotel> hotelList = new ArrayList<Hotel>();
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
					.parse(new FileReader(HOTELS_FILE));
			
			for (CSVRecord record : records) {
				Integer id = Integer.parseInt(record.get("id"));
				Integer cityId = Integer.parseInt(record.get("city_id"));
				String name = record.get("name");
				Integer rating = Integer.parseInt(record.get("rating"));
				Integer stars = Integer.parseInt(record.get("stars"));
				Hotel newHotel = new Hotel(id, name, cityId, rating, stars);
				hotelList.add(newHotel);
			}

			return hotelList;
		}
		
		/**
		 * Loads the advertisers.
		 * 
		 * @return List of advertisers
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		static public List<Advertiser> loadAdvertisers() throws FileNotFoundException, IOException {
			List<Advertiser> advertiserList = new ArrayList<Advertiser>();
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
					.parse(new FileReader(ADVERTISER_FILE));
			
			for (CSVRecord record : records) {
				Integer id = Integer.parseInt(record.get("id"));
				String advertiserName = record.get("advertiser_name");
				Advertiser newAdvertiser = new Advertiser(id, advertiserName);
				advertiserList.add(newAdvertiser);
			}

			return advertiserList;
		}
		
		/**
		 * Loads the relationship between advertisers and the hotels they have offers for.
		 * 
		 * @return A map of advertisers to hotels
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		static public Map<Integer, List<Integer>> loadAdvertiserIdToHotelIds() throws FileNotFoundException, IOException {
			Map<Integer, List<Integer>> advertiserIdToHotelIds = new HashMap<>();
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
					.parse(new FileReader(HOTEL_ADVERTISER_FILE));
			
			for (CSVRecord record : records) {
				Integer advertiserId = Integer.parseInt(record.get("advertiser_id"));
				Integer hotelId = Integer.parseInt(record.get("hotel_id"));

				List<Integer> hotelIds = advertiserIdToHotelIds.get(advertiserId);
				if (hotelIds == null) {
					hotelIds = new ArrayList<Integer>();
				}
				hotelIds.add(hotelId);

				advertiserIdToHotelIds.put(advertiserId, hotelIds);
			}
			
			return advertiserIdToHotelIds;
			
		}
		
		/**
		 * Loads the cities.
		 * 
		 * @return A map of city ids to city names 
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		static public Map<Integer, String> loadCityIdToCityName() throws FileNotFoundException, IOException {
			Map<Integer, String> cityMap = new HashMap<Integer, String>();
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
					.parse(new FileReader(CITIES_FILE));
			
			for (CSVRecord record : records) {
				Integer id = Integer.parseInt(record.get("id"));
				String cityName = record.get("city_name");
				cityMap.put(id, cityName);
			}

			return cityMap;
		}
	}
}
