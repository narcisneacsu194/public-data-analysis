package com.teamtreehouse.publicdata.dao;

import com.teamtreehouse.publicdata.model.Country;

import java.util.List;

public interface CountryDAO {
    List<Country> fetchAllCountries();
    Country getCountryByCode(String code);
    void addCountry(Country country);
    void updateCountry(Country country);
    void deleteCountry(Country country);
}
