package com.kkt.worthcalculation.handle

import com.kkt.worthcalculation.model.ErrorDetails
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class CustomerExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {

        val fieldErrors: List<FieldError> = ex.fieldErrors
        val errorMapping = fieldErrors.associate { it.field to it.defaultMessage }

        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = "Validation Failed",
            status = "error",
            data = errorMapping
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    override fun handleMissingServletRequestParameter(ex: MissingServletRequestParameterException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {

        val fieldErrors = ex.parameterName

        val errorDetails = ErrorDetails(
            timestamp = LocalDateTime.now(),
            message = "Validation Failed",
            status = "error",
            data = "Required $fieldErrors"
        )
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }
}

