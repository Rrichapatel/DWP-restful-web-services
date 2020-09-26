package com.dwp.rest.webservices.DWPrestfulwebservice;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.dwp.rest.webservices.model.User;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

@RestController
@RequestMapping("/dwp")
public class UserResource {

	@Autowired
	private RestTemplate restTemplate;

	public static final String API_BASE_URL = "https://bpdts-test-app.herokuapp.com";

	/*
	 * fetch all the users --> filter list based on Longitude and Latitude values to
	 * get list of users who lives in 50 miles range of London. Make another API
	 * call to get list of users who lives in London. get user detail Merge both the
	 * list and return the result.
	 */
	@GetMapping("/users")
	public List<User> retrieveQualifiedUsers() {

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		String url = API_BASE_URL + "/users";

		ResponseEntity<List<User>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
				new ParameterizedTypeReference<List<User>>() {
				});

		List<User> users = response.getBody();

		return Stream.concat(getUserWithin50MilesOfLondon(users).stream(), getUserOfLondon().stream())
				.collect(Collectors.toList());

	}

	private List<User> getUserWithin50MilesOfLondon(List<User> users) {

		/*
		 * Before calculating anything, first we need to define what is “within a 50
		 * mile radius” means. Within 50 miles of what, exactly? The center of London?
		 * 
		 * Wikipedia says that London is at 51°30′26″N 0°7′39″W, which seems like as
		 * reasonable a starting point as any for distance calculations.
		 * 
		 * Traditionally, latitude and longitude are expressed in decimal degrees when
		 * doing calculations, which means a double precision floating point number
		 * (that is, a Java double) whose integer part is the number of degrees, and
		 * whose decimal part is the minutes and seconds. By convention, positive values
		 * are north or east, while negative values are south or west.
		 * 
		 * Thus, 50°30′N 99°15′W is a latitude of 50.5 and a longitude of -99.25, in
		 * decimal degrees.
		 * 
		 * Once we have London’s location in decimal degrees, we can invoke the Inverse
		 * method of the Geodesic class.
		 * 
		 * GeodesicData result = Geodesic.WGS84.Inverse(londonLat, londonLon, userLat,
		 * userLon);
		 */

		// 51 deg 30 min 26 sec N
		final double londonLatitude = 51 + (30 / 60.0) + (26 / 60.0 / 60.0);

		// 0 deg 7 min 39 sec W
		final double londonLongitude = 0 - (7 / 60.0) - (39 / 60.0 / 60.0);

		Predicate<User> filterEligibleUser = (user) -> {

			GeodesicData result = Geodesic.WGS84.Inverse(londonLatitude, londonLongitude, user.getLatitude(),
					user.getLongitude());

			double distanceInMeters = result.s12;
			double distanceInMiles = distanceInMeters / 1609.34;

			if (distanceInMiles <= 50) {
				return true;
			}
			return false;
		};
		return users.stream().filter(filterEligibleUser).collect(Collectors.toList());

	}

	private List<User> getUserOfLondon() {

		ResponseEntity<List<User>> response = restTemplate.exchange(API_BASE_URL + "/city/" + "London" + "/users",
				HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
				});

		return response.getBody();
	}

}
