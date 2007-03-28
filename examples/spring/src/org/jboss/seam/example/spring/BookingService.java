package org.jboss.seam.example.spring;


import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Example of using the JpaDaoSupport.
 *
 * @author Mike Youngstrom
 */
public class BookingService 
    extends JpaDaoSupport 
{
    @SuppressWarnings("unchecked")
    @Transactional
    public List<Hotel> findHotels(final String searchPattern, final int firstResult, final int maxResults) {
        return getJpaTemplate().executeFind(new JpaCallback() {
        	public Object doInJpa(EntityManager em) throws PersistenceException {
                return em.createQuery("select h from Hotel h where lower(h.name) like :search or lower(h.city) like :search or lower(h.zip) like :search or lower(h.address) like :search")
                .setParameter("search", searchPattern)
                .setMaxResults(maxResults)
                .setFirstResult(firstResult)
                .getResultList();
        	}
        });
    }



    @SuppressWarnings("unchecked")
    @Transactional
    public List<Booking> findBookingsByUsername(String username) {
        return getJpaTemplate().findByNamedParams("select b from Booking b where b.user.username = :username order by b.checkinDate",
                                                  Collections.singletonMap("username", username));

    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("BookingId cannot be null");
        }

        Booking cancelled = getJpaTemplate().find(Booking.class, bookingId);
        if (cancelled != null) {
            getJpaTemplate().remove(cancelled);
        }
    }

    public void validateBooking(Booking booking) 
        throws ValidationException 
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        if (booking.getCheckinDate().before(calendar.getTime())) {
            throw new ValidationException("Check in date must be a future date");
        } else if (!booking.getCheckinDate().before(booking.getCheckoutDate())) {
            throw new ValidationException("Check out date must be later than check in date");
        }
    }



    @Transactional
    public void bookHotel(Booking booking) 
        throws ValidationException 
    {
        validateBooking(booking);

        getJpaTemplate().persist(booking);
        getJpaTemplate().flush();
    }
    
    @Transactional
    public void testNonWebRequest() {
    	List<Hotel> hotels = findHotels("%", 0, 1);
    	if(!hotels.isEmpty()) {
    		System.out.println("Asynchronously found hotel: "+hotels.get(0).getName());
    		return;
    	}
    	System.out.println("No Hotels Found.");
    }

    @Transactional
    public Hotel findHotelById(Long hotelId) {
        if (hotelId == null) {
            throw new IllegalArgumentException("hotelId cannot be null");
        }

        return getJpaTemplate().find(Hotel.class, hotelId);
    }
}
