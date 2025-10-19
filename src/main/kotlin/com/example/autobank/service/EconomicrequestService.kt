package com.example.autobank.service

import com.example.autobank.data.models.Economicrequest
import com.example.autobank.repository.EconomicrequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class EconomicrequestService {

    @Autowired
    lateinit var economicrequestRepository: EconomicrequestRepository

    @Autowired
    lateinit var onlineUserService: OnlineUserService


    fun deleteEconomicrequest(id: Int) {
        economicrequestRepository.deleteById(id);
    }

    fun getEconomicrequest(id: Int): Economicrequest {
        return economicrequestRepository.findById(id).get();
    }


//    fun updateEconomicrequest(economicrequest: Economicrequest, id: Int) {
//       val economicrequestToUpdate = economicrequestRepository.findById(id).get();
//        economicrequest.id = economicrequestToUpdate.id;
//        economicrequestRepository.save(economicrequest);
//    }

}