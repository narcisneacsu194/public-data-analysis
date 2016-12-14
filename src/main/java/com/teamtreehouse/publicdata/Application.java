package com.teamtreehouse.publicdata;

import com.teamtreehouse.publicdata.model.Country;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.List;

public class Application {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory(){
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void main(String[] args){
        System.out.printf("Country\t\t\t\t\t\t\t\tInternet Users\t\t\tLiteracy%n");
        System.out.printf("--------------------------------------------------------------------%n");
        for(Country country : fetchAllCountries()){
            System.out.printf("%s\t\t\t\t\t\t\t\t%.2f\t\t\t%.2f",
                    country.getName(), country.getInternetUsers(),
                    country.getAdultLiteracyRate());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Country> fetchAllCountries(){
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Country.class);
        List<Country> countries = criteria.list();
        session.close();
        return countries;
    }
}
