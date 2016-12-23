package com.teamtreehouse.publicdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.teamtreehouse.publicdata.dao.SimpleCountryDAO;
import com.teamtreehouse.publicdata.model.Country;

public class Prompter {
    private BufferedReader bufferedReader;
    private SimpleCountryDAO simpleCountryDAO;

    public Prompter(){
        bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        simpleCountryDAO = new SimpleCountryDAO();
    }

    public void run(){
        int choice;
        boolean quitVariable = false;

        while(true){
            System.out.printf("%n%n1. View data table%n2. View statistics%n3. Add a country%n");
            System.out.printf("4. Edit a country%n5. Delete a country%n6. Quit%n%n");
            System.out.printf("Choose:  ");

            try {
                choice = Integer.parseInt(bufferedReader.readLine());
                if(choice < 1 || choice > 6){
                    System.out.printf("%nThe value you entered is not between the 1-6 range.%n");
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
                    case 6:
                        quitVariable = true;
                }

            }catch(IOException ioe){
                System.out.printf("%nSomething went wrong with the stream.%n");
                ioe.printStackTrace();
                System.exit(0);
            }catch(IllegalArgumentException iae){
                System.out.printf("%nYou are only allowed to pass in numerical values.%n");
            }

            if(quitVariable){
                System.exit(0);
            }
        }

    }

    // Lists all the rows of the Country table
    private void viewCountries(){
        System.out.printf("%nCode\t\t\t\tCountry\t\t\t\t\t\t\t\tInternet Users\t\t\t\tLiteracy%n");
        System.out.printf("-----------------------------------------------------------------------------------------------%n");
        for(Country country : simpleCountryDAO.fetchAllCountries()){
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

    private Double promptForPercent(String column, String action) throws IOException, IllegalArgumentException{
        String response;
        Double percent = null;
        System.out.printf("%nDo you want to %s the value for the %s column ? (YES/any other value)  ", column, action);
        response = bufferedReader.readLine();
        response = response.toUpperCase();
        if(response.equals("Y") || response.equals("YES")){
            System.out.printf("%nEnter an %s value (must be a decimal value between 0 and 100):  ", column);
            percent = Double.parseDouble(bufferedReader.readLine());

            if(percent < 0.0 || percent > 100.0){
                System.out.printf("%nThe value you entered is not between the 0.0-100.0 range.%n");
                System.out.printf("It is assumed that you didn't enter a value at all for the %s column.%n", column);
                percent = null;
            }
        }

        return percent;
    }

    // This method prompts you to put information for a new country that will be added to the database
    private void addCountry() throws IOException, IllegalArgumentException{
        Country country;
        String code, countryName;
        Double internetUsers, adultLiteracy;

        while(true){
            System.out.printf("%nEnter a country code (it must be of the form AZB, ROM and so on):  ");
            code = bufferedReader.readLine();

            if(!code.matches("[a-zA-Z]+")){
                System.out.printf("%nThe country code must contain only letters, and must not be blank.%n");
                continue;
            }

            if(code.length() > 3){
                System.out.printf("%nThe country code must contain only three letters.%n");
                continue;
            }

            code = code.toUpperCase();

            System.out.printf("%nEnter a country name:  ");

            countryName = bufferedReader.readLine();

            if(!countryName.matches("[a-zA-Z]+")){
                System.out.printf("%nThe country name must contain only letters, and must not be blank.%n");
                continue;
            }

            countryName = countryName.substring(0, 1).toUpperCase() + countryName.substring(1).toLowerCase();

            internetUsers = promptForPercent("Internet Users", "enter");
            adultLiteracy = promptForPercent("Adult Literacy", "enter");

            country = new Country(new Country
                    .CountryBuilder(code, countryName).
                    withInternetUsers(internetUsers).
                    withAdultLiteracyRate(adultLiteracy));
            simpleCountryDAO.addCountry(country);
            break;
        }
    }

    // This method prompts you to put new information for a selected country from the database
    private void updateCountry() throws IOException, IllegalArgumentException{
        String code, response, countryName;
        Country country;
        Double internetUsers, adultLiteracy;

        while(true){
            System.out.printf("%nEnter the code of the country you want to edit (it must be of the form AZB, ROM and so on):  ");
            code = bufferedReader.readLine();

            if(!code.matches("[a-zA-Z]+")){
                System.out.printf("%nThe country code must contain only letters, and must not be blank.%n");
                continue;
            }

            if(code.length() > 3){
                System.out.printf("%nThe country code must contain only three letters.%n");
                continue;
            }

            code = code.toUpperCase();

            country = simpleCountryDAO.getCountryByCode(code);
            if(country == null){
                System.out.printf("%nThe country you tried to get is not in the database. Try again.%n");
                continue;
            }

            System.out.printf("%nDo you want to edit the country name ? (YES/any other value):  ");
            response = bufferedReader.readLine();
            response = response.toUpperCase();

            if(response.equals("Y") || response.equals("YES")){
                System.out.printf("%nEnter a new name for country %s:  ", country.getName());
                countryName = bufferedReader.readLine();

                if(!countryName.matches("[a-zA-Z]+")){
                    System.out.printf("%nThe country name must contain only letters, and must not be blank.%n");
                    continue;
                }

                countryName = countryName.substring(0, 1).toUpperCase() + countryName.substring(1).toLowerCase();

                country.setName(countryName);
            }

            internetUsers = promptForPercent("Internet Users", "edit");
            adultLiteracy = promptForPercent("Adult Literacy", "edit");
            country.setInternetUsers(internetUsers);
            country.setAdultLiteracyRate(adultLiteracy);

            simpleCountryDAO.updateCountry(country);
            break;
        }
    }

    // This method prompts you for a code of a country you want to remove from the database
    private void deleteCountry() throws IOException{
        Country country;
        String code;

        while(true){
            System.out.printf("%nEnter the code of the country you want to delete (it must be of the form AZB, ROM and so on):  ");
            code = bufferedReader.readLine();

            if(!code.matches("[a-zA-Z]+")){
                System.out.printf("%nThe country code must contain only letters, and must not be blank.%n");
                continue;
            }

            if(code.length() > 3){
                System.out.printf("%nThe country code must contain only three letters.%n");
                continue;
            }

            code = code.toUpperCase();

            country = simpleCountryDAO.getCountryByCode(code);
            if(country == null){
                System.out.printf("%nThe country you tried to get is not in the database. Try again.%n");
                continue;
            }

            simpleCountryDAO.deleteCountry(country);
            break;
        }
    }

    // This method prints out the follwoing statistics: min, max of Internet usage and adult literacy,
    // and the correlation coefficient between the two columns
    private void viewStatistics(){
        Double correlationCoefficient = simpleCountryDAO.getCorrelationCoefficient();
        Country country = simpleCountryDAO.getCountryWithMaxInternetUsage();
        System.out.printf("%nCountry with greatest internet usage percent: %s --> %.2f%n",
                country.getName(), country.getInternetUsers());
        country = simpleCountryDAO.getCountryWithMinInternetUsage();
        System.out.printf("%nCountry with least internet usage percent: %s --> %.2f%n",
                country.getName(), country.getInternetUsers());
        country = simpleCountryDAO.getCountryWithMaxAdultLiteracy();
        System.out.printf("%nCountry with greatest adult literacy percent: %s --> %.2f%n",
                country.getName(), country.getAdultLiteracyRate());
        country = simpleCountryDAO.getCountryWithMinAdultLiteracy();
        System.out.printf("%nCountry with least adult literacy percent: %s --> %.2f%n",
                country.getName(), country.getAdultLiteracyRate());
        System.out.printf("Correlation coefficient (Internet Usage <-> Adult Literacy): %.2f",
                correlationCoefficient);
    }
}
