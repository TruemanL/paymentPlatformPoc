package com.paymentPlatformPoc.adaptor

import io.grpc.Status
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {
    @GrpcExceptionHandler
    fun handleInvalidArgument(e: IllegalArgumentException): Status {
        return Status.INVALID_ARGUMENT.withDescription(e.message)
    }
}