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
        List<Country> countries = fetchAllCountries();
        printCountries(countries);

    }

    private static void printCountries(List<Country> countries){
        System.out.printf("Country\t\t\t\t\t\t\t\tInternet Users\t\t\tLiteracy%n");
        System.out.printf("----------------------------------------------------------------------%n");
        for(Country country : countries){
            System.out.printf("%-40s", country.getName());
            if(country.getInternetUsers() == null){
                System.out.printf("--\t\t\t\t\t\t");
            }else{
                System.out.printf("%.2f\t\t\t\t\t", country.getInternetUsers());
            }

            if(country.getAdultLiteracyRate() == null){
                System.out.printf("--%n");
            }else{
                System.out.printf("%.2f%n", country.getAdultLiteracyRate());
            }
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
