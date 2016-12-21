package com.teamtreehouse.publicdata;

import com.teamtreehouse.publicdata.model.Country;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Application {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Builds the session factory. A session factory is used to generate database sessions.
    // A session is constituted of one or more SQL statements executed at a time.
    private static SessionFactory buildSessionFactory(){
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void main(String[] args){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        int choice = 0;

        while(true){
            System.out.printf("%n%n1. View data table%n2. View statistics%n3. Add a country%n");
            System.out.printf("4. Edit a country%n5. Delete a country%n%n");
            System.out.printf("Choose:  ");

            try {
                choice = Integer.parseInt(bufferedReader.readLine());
                if(choice < 1 || choice > 5){
                    System.out.printf("%nThe value you entered is not between the 1-5 range.%n");
                }
            }catch(IOException ioe){
                System.out.printf("%nSomething went wrong with the stream.%n");
                ioe.printStackTrace();
                System.exit(0);
            }catch(IllegalArgumentException iae){
                System.out.printf("%nYou are only allowed to pass in numerical values.%n");
            }

            switch(choice){
                case 1:
                    viewCountries();
                    break;
                case 2:
                    viewStatistics();
                    break;
                case 3:
                    addCountry();
                    break;
                case 4:
                    updateCountry();
                    break;
                case 5:
                    deleteCountry();
                    break;
            }
        }

    }

    // Lists all the rows of the Country table
    private static void viewCountries(){
        List<Country> countries = fetchAllCountries();
        System.out.printf("%nCode\t\t\t\tCountry\t\t\t\t\t\t\t\tInternet Users\t\t\t\tLiteracy%n");
        System.out.printf("-----------------------------------------------------------------------------------------------%n");
        for(Country country : countries){
            System.out.printf("%-20s", country.getCode());
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

    // Gets all the country objects available in the database.
    @SuppressWarnings("unchecked")
    private static List<Country> fetchAllCountries(){
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Country.class);
        List<Country> countries = criteria.list();
        session.close();
        return countries;
    }

    // Gets a country object by a unique code you specify
    private static Country getCountryByCode(String code){
        Session session = sessionFactory.openSession();
        Country country = session.get(Country.class, code);
        session.close();
        return country;
    }

    private static void addCountry(){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Country country;
        String code;
        String countryName;
        String response;
        Double internetUsers = null;
        Double adultLiteracy = null;

        try{
            while(true){
                System.out.printf("%nEnter a country code (it must be the form AZB, ROM and so on): ");
                code = bufferedReader.readLine();

                System.out.printf("%nEnter a country name%n");

                countryName = bufferedReader.readLine();

                System.out.printf("%nDo you want to enter a value for the Internet Users column ? (YES/any other value)  ");
                response = bufferedReader.readLine();
                response = response.toUpperCase();
                if(response.equals("Y") || response.equals("YES")){
                    System.out.printf("%nEnter an Internet Users value (must be a decimal value between 0 and 100):  ");
                    internetUsers = Double.parseDouble(bufferedReader.readLine());

                    if(internetUsers < 0.0 || internetUsers > 100.0){
                        System.out.printf("%nThe value you entered is not between the 0.0-100.0 range.%n");
                        System.out.printf("It is assumed that you didn't enter a value at all for the Internet Users column.%n");
                        internetUsers = null;
                    }
                }

                System.out.printf("%nDo you want to enter a value for the Literacy column ? (YES/any other value)  ");
                response = bufferedReader.readLine();
                response = response.toUpperCase();
                if(response.equals("Y") || response.equals("YES")){
                    System.out.printf("%nEnter a Literacy value (must be a decimal value between 0 and 100):  ");
                    adultLiteracy = Double.parseDouble(bufferedReader.readLine());

                    if(adultLiteracy < 0.0 || adultLiteracy > 100.0){
                        System.out.printf("%nThe value you entered is not between the 0.0-100.0 range.%n");
                        System.out.printf("It is assumed that you didn't enter a value at all for the Literacy column.%n");
                        adultLiteracy = null;
                    }
                }

                if(code != null && countryName != null){
                    country = new Country(new Country
                            .CountryBuilder(code, countryName).
                            withInternetUsers(internetUsers).
                            withAdultLiteracyRate(adultLiteracy));
                    addCountryToDatabase(country);
                    break;
                }else{
                    System.out.printf("%nYou didn't provide a code or name for the country.%n");
                }
            }

        }catch(IOException ioe){
            System.out.printf("%nSomething went wrong with the stream.%n");
            ioe.printStackTrace();
            System.exit(0);
        }catch(IllegalArgumentException iae){
            System.out.printf("%nYou are only allowed to pass in numerical values.%n");
        }
    }

    // Adds a country object to the database
    private static void addCountryToDatabase(Country country){
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(country);
        session.getTransaction().commit();
        session.close();
    }

    private static void updateCountry(){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String code;
        Country country;
        String response;
        String name;
        Double internetUsers;
        Double adultLiteracy;

        try{
            while(true){
                System.out.printf("%nEnter the code of the country you want to edit:  ");
                code = bufferedReader.readLine();

                if(code.equals("")){
                    System.out.printf("%nYou didn't enter any code value%n");
                    continue;
                }

                country = getCountryByCode(code);
                if(country == null){
                    System.out.printf("%nThe country you tried to get is not in the database. Try again.%n");
                    continue;
                }

                System.out.printf("%nDo you want to edit the country name ? (YES/any other value):  ");
                response = bufferedReader.readLine();
                response = response.toUpperCase();

                if(response.equals("Y") || response.equals("YES")){
                    System.out.printf("%nEnter a new name for the selected country:  ");
                    name = bufferedReader.readLine();
                    if(name.equals("")){
                        System.out.printf("%nYou didn't enter anything. Try again.%n");
                        continue;
                    }

                    country.setName(name);
                }

                System.out.printf("%nDo you want to edit the Internet Usage value ? (YES/any other value):  ");
                response = bufferedReader.readLine();
                response = response.toUpperCase();
                if(response.equals("Y") || response.equals("YES")){
                    System.out.printf("%nEnter an Internet Users value (must be a decimal value between 0 and 100):  ");
                    internetUsers = Double.parseDouble(bufferedReader.readLine());

                    if(internetUsers < 0.0 || internetUsers > 100.0){
                        System.out.printf("%nThe value you entered is not between the 0.0-100.0 range.%n");
                        System.out.printf("It is assumed that you didn't enter a value at all for the Internet Users column.%n");
                        internetUsers = null;
                    }

                    country.setInternetUsers(internetUsers);
                }else{
                    System.out.printf("%nIt is assumed that you do not wish to add an Internet Users value for your country.%n");
                }

                System.out.printf("%nDo you want to edit the Literacy value ? (YES/any other value):  ");
                response = bufferedReader.readLine();
                response = response.toUpperCase();
                if(response.equals("Y") || response.equals("YES")){
                    System.out.printf("%nEnter a Literacy value (must be a decimal value between 0 and 100):  ");
                    adultLiteracy = Double.parseDouble(bufferedReader.readLine());

                    if(adultLiteracy < 0.0 || adultLiteracy > 100.0){
                        System.out.printf("%nThe value you entered is not between the 0.0-100.0 range.%n");
                        System.out.printf("It is assumed that you didn't enter a value at all for the Literacy column.%n");
                    }

                    country.setAdultLiteracyRate(adultLiteracy);
                }else{
                    System.out.printf("%nIt is assumed that you do not wish to add an Literacy value for your country.%n");
                }

                updateCountryFromDatabase(country);
                break;
            }
        }catch(IOException ioe){
            System.out.printf("%nSomething went wrong with the stream.%n");
            ioe.printStackTrace();
            System.exit(0);
        }catch(IllegalArgumentException iae){
            System.out.printf("%nYou are only allowed to pass in numerical values.%n");
        }
    }

    // Persists the changes you made to a specified country object
    private static void updateCountryFromDatabase(Country country){
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(country);
        session.getTransaction().commit();
        session.close();
    }

    private static void deleteCountry(){
        Country country;
        String code;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        try{
            while(true){
                System.out.printf("%nEnter the code of the country you want to delete:  ");
                code = bufferedReader.readLine();

                if(code.equals("")){
                    System.out.printf("%nYou didn't enter any code value%n");
                    continue;
                }

                country = getCountryByCode(code);
                if(country == null){
                    System.out.printf("%nThe country you tried to get is not in the database. Try again.%n");
                    continue;
                }

                deleteCountryFromDatabase(country);
                break;
            }
        }catch(IOException ioe){
            System.out.printf("%nSomething went wrong with the stream.%n");
            ioe.printStackTrace();
            System.exit(0);
        }

    }

    // Deletes a country of your choice
    private static void deleteCountryFromDatabase(Country country){
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(country);
        session.getTransaction().commit();
        session.close();
    }

    private static void viewStatistics(){
        Double correlationCoefficient = getCorrelationCoefficient();
        Country country = getCountryWithMaxInternetUsage();
        System.out.printf("%nCountry with greatest internet usage percent: %s --> %.2f%n",
                country.getName(), country.getInternetUsers());
        country = getCountryWithMinInternetUsage();
        System.out.printf("%nCountry with least internet usage percent: %s --> %.2f%n",
                country.getName(), country.getInternetUsers());
        country = getCountryWithMaxAdultLiteracy();
        System.out.printf("%nCountry with greatest adult literacy percent: %s --> %.2f%n",
                country.getName(), country.getAdultLiteracyRate());
        country = getCountryWithMinAdultLiteracy();
        System.out.printf("%nCountry with least adult literacy percent: %s --> %.2f%n",
                country.getName(), country.getAdultLiteracyRate());
        System.out.printf("Correlation coefficient (Internet Usage <-> Adult Literacy): %.2f",
                correlationCoefficient);
    }

    // Find the country with the maximum Internet Usage percentage.
    private static Country getCountryWithMaxInternetUsage(){
        List<Country> countries = fetchAllCountries();
        List<Country> nonNullInternetUsageCountries = new ArrayList<>();
        Country topCountryByInternetUsage;

        for(Country country : countries){
            if(country.getInternetUsers() != null){
                nonNullInternetUsageCountries.add(country);
            }
        }

        topCountryByInternetUsage = nonNullInternetUsageCountries.stream().max((o1, o2) -> {
            if(o1.getInternetUsers() > o2.getInternetUsers()){
                return 1;
            }else if(o1.getInternetUsers() < o2.getInternetUsers()){
                return -1;
            }
            return 0;
        }).get();

        return topCountryByInternetUsage;
    }

    // Find the country with the minimum Internet Usage percentage.
    private static Country getCountryWithMinInternetUsage(){
        List<Country> countries = fetchAllCountries();
        List<Country> nonNullInternetUsageCountries = new ArrayList<>();
        Country bottomCountryByInternetUsage;

        for(Country country : countries){
            if(country.getInternetUsers() != null){
                nonNullInternetUsageCountries.add(country);
            }
        }

        bottomCountryByInternetUsage = nonNullInternetUsageCountries.stream().max((o1, o2) -> {
            if(o1.getInternetUsers() > o2.getInternetUsers()){
                return -1;
            }else if(o1.getInternetUsers() < o2.getInternetUsers()){
                return 1;
            }
            return 0;
        }).get();

        return bottomCountryByInternetUsage;
    }

    // Find the country with the maximum Adult Literacy percentage.
    private static Country getCountryWithMaxAdultLiteracy(){
        List<Country> countries = fetchAllCountries();
        List<Country> nonNullAdultLiteracyCountries = new ArrayList<>();
        Country topCountryByAdultLiteracy;

        for(Country country : countries){
            if(country.getAdultLiteracyRate() != null){
                nonNullAdultLiteracyCountries.add(country);
            }
        }

        topCountryByAdultLiteracy = nonNullAdultLiteracyCountries.stream().max((o1, o2) -> {
            if(o1.getAdultLiteracyRate() > o2.getAdultLiteracyRate()){
                return 1;
            }else if(o1.getAdultLiteracyRate() < o2.getAdultLiteracyRate()){
                return -1;
            }
            return 0;
        }).get();

        return topCountryByAdultLiteracy;
    }

    // Find the country with the minimum Adult Literacy percentage.
    private static Country getCountryWithMinAdultLiteracy(){
        List<Country> countries = fetchAllCountries();
        List<Country> nonNullAdultLiteracyCountries = new ArrayList<>();
        Country bottomCountryByAdultLiteracy;

        for(Country country : countries){
            if(country.getAdultLiteracyRate() != null){
                nonNullAdultLiteracyCountries.add(country);
            }
        }

        bottomCountryByAdultLiteracy = nonNullAdultLiteracyCountries.stream().max((o1, o2) -> {
            if(o1.getAdultLiteracyRate() > o2.getAdultLiteracyRate()){
                return -1;
            }else if(o1.getAdultLiteracyRate() < o2.getAdultLiteracyRate()){
                return 1;
            }
            return 0;
        }).get();

        return bottomCountryByAdultLiteracy;
    }

    // Get the correlation coefficient using the Internet Usage and Adult Literacy column values
    private static double getCorrelationCoefficient(){
        Double correlationCoefficient;
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

        correlationCoefficient = product / Math.sqrt(internetUsageSquared * adultLiteracySquared);

        return correlationCoefficient;
    }

    // Gets the mean value of the Internet Usage column
    private static double getInternetUsageMeanValue(){
        List<Country> countries = fetchAllCountries();
        double internetUsageMeanValue = 0.0;
        double counter = 0.0;

        for(Country country : countries){
            if(country.getInternetUsers() != null){
                internetUsageMeanValue += country.getInternetUsers();
                counter += 1.0;
            }
        }

        internetUsageMeanValue /= counter;

        return internetUsageMeanValue;
    }

    // Gets the mean value of the Adult Literacy column
    private static double getAdultLiteracyMeanValue(){
        List<Country> countries = fetchAllCountries();
        double adultLiteracyMeanValue = 0.0;
        double counter = 0.0;

        for(Country country : countries){
            if(country.getAdultLiteracyRate() != null){
                adultLiteracyMeanValue += country.getAdultLiteracyRate();
                counter += 1.0;
            }
        }

        adultLiteracyMeanValue /= counter;

        return adultLiteracyMeanValue;
    }


    private static List<Double> getInternetUsageValuesSubtractedByMeanList(){
        List<Double> internetUsageList = new ArrayList<>();
        List<Country> countries = fetchAllCountries();

        for(Country country : countries){
            if(country.getInternetUsers() != null && country.getAdultLiteracyRate() != null){
                internetUsageList.add(
                        country.getInternetUsers() -
                                getInternetUsageMeanValue());
            }
        }

        return internetUsageList;
    }

    private static List<Double> getAdultLiteracyValuesSubtractedByMeanList(){
        List<Double> adultLiteracyList = new ArrayList<>();
        List<Country> countries = fetchAllCountries();

        for(Country country : countries){
            if(country.getInternetUsers() != null && country.getAdultLiteracyRate() != null){
                adultLiteracyList.add(
                        country.getAdultLiteracyRate() -
                                getAdultLiteracyMeanValue());
            }
        }

        return adultLiteracyList;
    }

}
