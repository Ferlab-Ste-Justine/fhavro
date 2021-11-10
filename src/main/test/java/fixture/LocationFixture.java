package fixture;

import org.hl7.fhir.r4.model.Location;

import java.util.Arrays;

public class LocationFixture {

    public static Location createLocation() {
        Location location = new Location();
        location.setAddress(AddressFixture.createAddress());
        location.setAvailabilityExceptions("AvailabilityException?");
        location.setEndpoint(Arrays.asList(ReferenceFixture.createAbsoluteReference()));
        return location;
    }
}
