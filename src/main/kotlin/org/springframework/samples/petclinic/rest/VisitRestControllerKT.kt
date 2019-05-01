package org.springframework.samples.petclinic.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.model.Visit
import org.springframework.samples.petclinic.service.ClinicService
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.ArrayList
import javax.transaction.Transactional
import javax.validation.Valid

@RestController
@CrossOrigin(exposedHeaders = ["errors, content-type"])
@RequestMapping("api/visits")
open class VisitRestControllerKT(
        @Autowired private val clinicService: ClinicService
) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getAllVisits(): ResponseEntity<Collection<Visit>> {
        val visits = ArrayList<Visit>()
        visits.addAll(this.clinicService.findAllVisits())
        return if (visits.isEmpty()) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        } else ResponseEntity(visits, HttpStatus.OK)
    }

    @GetMapping("/{visitId}", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getVisit(@PathVariable("visitId") visitId: Int): ResponseEntity<Visit> {
        val visit = this.clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(visit, HttpStatus.OK)
    }


    @PostMapping("", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun addVisit(@RequestBody @Valid visit: Visit?, bindingResult: BindingResult, ucBuilder: UriComponentsBuilder): ResponseEntity<Visit> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || visit == null || visit.pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity(headers, HttpStatus.BAD_REQUEST)
        }
        this.clinicService.saveVisit(visit)
        headers.location = ucBuilder.path("/api/visits/{id}").buildAndExpand(visit.id!!).toUri()
        return ResponseEntity(visit, headers, HttpStatus.CREATED)
    }

    @PutMapping("/{visitId}", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun updateVisit(@PathVariable("visitId") visitId: Int, @RequestBody @Valid visit: Visit?, bindingResult: BindingResult): ResponseEntity<Visit> {
        val errors = BindingErrorsResponse()
        val headers = HttpHeaders()
        if (bindingResult.hasErrors() || visit == null || visit.pet == null) {
            errors.addAllErrors(bindingResult)
            headers.add("errors", errors.toJSON())
            return ResponseEntity(headers, HttpStatus.BAD_REQUEST)
        }
        val currentVisit = this.clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        currentVisit.date = visit.date
        currentVisit.description = visit.description
        currentVisit.pet = visit.pet
        this.clinicService.saveVisit(currentVisit)
        return ResponseEntity(currentVisit, HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/{visitId}", produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    @Transactional
    open fun deleteVisit(@PathVariable("visitId") visitId: Int): ResponseEntity<Void> {
        val visit = this.clinicService.findVisitById(visitId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        this.clinicService.deleteVisit(visit)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
