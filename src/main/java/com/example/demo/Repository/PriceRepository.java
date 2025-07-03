package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price, Integer>{

    Price findBySourceCityAndSourceStateAndDestinationCityAndDesitnationState(String sourceCity, String sourceState, String destinationCity, String desitnationState);

    
}
