package com.teamtreehouse.publicdata.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import java.util.List;

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
}
