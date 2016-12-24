package com.teamtreehouse.publicdata.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.teamtreehouse.publicdata.model.Country;

public class SimpleCountryDAO implements CountryDAO{
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Builds the session factory. A session factory is used to generate database sessions.
    // A session is constituted of one or more SQL statements executed at a time.
    private static SessionFactory buildSessionFactory(){
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    // Gets all the country objects available in the database.
    @Override
    @SuppressWarnings("unchecked")
    public List<Country> fetchAllCountries() {
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Country.class);
        List<Country> countries = criteria.list();
        session.close();

        return countries;
    }

    // Gets a country object by a unique code you specify
    @Override
    public Country getCountryByCode(String code) {
        Session session = sessionFactory.openSession();
        Country country = session.get(Country.class, code);
        session.close();
        return country;
    }

    // Adds a country object to the database
    @Override
    public void addCountry(Country country) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(country);
        session.getTransaction().commit();
        session.close();
    }

    // Persists the changes you made to a specified country object
    @Override
    public void updateCountry(Country country) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(country);
        session.getTransaction().commit();
        session.close();
    }

    // Deletes a country of your choice
    @Override
    public void deleteCountry(Country country) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(country);
        session.getTransaction().commit();
        session.close();
    }

    // Gets a list of all the countries that have an Internet Users value available
    private List<Country> getNonNullInternetUsersCountries(){
        return fetchAllCountries()
                .stream()
                .filter(country -> country.getInternetUsers() != null)
                .collect(Collectors.toList());
    }

    // Find the country with the maximum Internet Usage percentage.
    @Override
    public Country getCountryWithMaxInternetUsage(){

        return getNonNullInternetUsersCountries().stream().max((o1, o2) -> {
            if(o1.getInternetUsers() > o2.getInternetUsers()){
                return 1;
            }else if(o1.getInternetUsers() < o2.getInternetUsers()){
                return -1;
            }
            return 0;
        }).get();

    }

    // Find the country with the minimum Internet Usage percentage.
    @Override
    public Country getCountryWithMinInternetUsage(){

        return getNonNullInternetUsersCountries().stream().max((o1, o2) -> {
            if(o1.getInternetUsers() > o2.getInternetUsers()){
                return -1;
            }else if(o1.getInternetUsers() < o2.getInternetUsers()){
                return 1;
            }
            return 0;
        }).get();

    }

    // Gets a list of all the countries that have an Adult Literacy rate value available.
    private List<Country> getNonNullAdultLiteracyRateCountries(){
        return fetchAllCountries()
                .stream()
                .filter(country -> country.getAdultLiteracyRate() != null)
                .collect(Collectors.toList());
    }

    // Find the country with the maximum Adult Literacy percentage.
    @Override
    public Country getCountryWithMaxAdultLiteracy(){

        return getNonNullAdultLiteracyRateCountries()
                .stream()
                .max((o1, o2) -> {
            if(o1.getAdultLiteracyRate() > o2.getAdultLiteracyRate()){
                return 1;
            }else if(o1.getAdultLiteracyRate() < o2.getAdultLiteracyRate()){
                return -1;
            }
            return 0;
        }).get();

    }

    // Find the country with the minimum Adult Literacy percentage.
    @Override
    public Country getCountryWithMinAdultLiteracy(){

        return getNonNullAdultLiteracyRateCountries()
                .stream()
                .max((o1, o2) -> {
            if(o1.getAdultLiteracyRate() > o2.getAdultLiteracyRate()){
                return -1;
            }else if(o1.getAdultLiteracyRate() < o2.getAdultLiteracyRate()){
                return 1;
            }
            return 0;
        }).get();
    }

    // Get the correlation coefficient using the Internet Usage and Adult Literacy column values
    @Override
    public double getCorrelationCoefficient(){
        List<Double> internetUsageList = getInternetUsageValuesSubtractedByMeanList();
        List<Double> adultLiteracyList = getAdultLiteracyValuesSubtractedByMeanList();
        Double product = 0.0;
        Double internetUsageSquared = 0.0;
        Double adultLiteracySquared = 0.0;

        for(int i = 0; i < internetUsageList.size();i++){
            product = product + internetUsageList.get(i) * adultLiteracyList.get(i);
            internetUsageSquared = internetUsageSquared + Math.pow(internetUsageList.get(i), 2);
            adultLiteracySquared = adultLiteracySquared + Math.pow(adultLiteracyList.get(i), 2);
        }

        return product / Math.sqrt(internetUsageSquared * adultLiteracySquared);
    }

    // Gets the mean value of the Internet Usage column
    private double getInternetUsageMeanValue(){
        double internetUsageMeanValue = 0.0;
        double counter = 0.0;

        for(Country country : getNonNullInternetUsersCountries()){
                internetUsageMeanValue += country.getInternetUsers();
                counter += 1.0;
        }

        return internetUsageMeanValue / counter;
    }

    // Gets the mean value of the Adult Literacy column
    private double getAdultLiteracyMeanValue(){
        double adultLiteracyMeanValue = 0.0;
        double counter = 0.0;

        for(Country country : getNonNullAdultLiteracyRateCountries()){
                adultLiteracyMeanValue += country.getAdultLiteracyRate();
                counter += 1.0;
        }

        return adultLiteracyMeanValue / counter;
    }

    private List<Country> getNonNullInternetUsageAndAdultLiteracyRateCountries(){
        return fetchAllCountries()
                .stream()
                .filter(country -> country.getInternetUsers() != null && country.getAdultLiteracyRate() != null)
                .collect(Collectors.toList());
    }

    //This method is used to calculate the correlation coefficient
    private List<Double> getInternetUsageValuesSubtractedByMeanList(){
        List<Double> internetUsageList = new ArrayList<>();

        for(Country country : getNonNullInternetUsageAndAdultLiteracyRateCountries()){
                internetUsageList.add(
                        country.getInternetUsers() -
                                getInternetUsageMeanValue());
        }

        return internetUsageList;
    }

    // This method is used to calculate the correlation coefficient
    private List<Double> getAdultLiteracyValuesSubtractedByMeanList(){
        List<Double> adultLiteracyList = new ArrayList<>();

        for(Country country : getNonNullInternetUsageAndAdultLiteracyRateCountries()){
                adultLiteracyList.add(
                        country.getAdultLiteracyRate() -
                                getAdultLiteracyMeanValue());
        }

        return adultLiteracyList;
    }
}
