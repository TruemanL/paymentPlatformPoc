package com.paymentPlatformPoc.adaptor

import com.paymentPlatformPoc.exception.InputOutOfRangeException
import io.grpc.Status
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {
    @GrpcExceptionHandler
    fun handleInvalidArgument(e: IllegalArgumentException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleOutOfRangeArgument(e: InputOutOfRangeException): Status {
        return Status.OUT_OF_RANGE.withDescription(e.message)
    }

    @GrpcExceptionHandler
    fun handleUnexpectedException(e: Exception): Status {
        return Status.UNKNOWN.withDescription(e.message)
    }
}