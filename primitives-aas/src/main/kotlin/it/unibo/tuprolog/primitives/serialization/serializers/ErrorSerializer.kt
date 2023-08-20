package it.unibo.tuprolog.primitives.serialization.serializers

import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.errors.DomainErrorMsg
import it.unibo.tuprolog.primitives.errors.EvaluationErrorMsg
import it.unibo.tuprolog.primitives.errors.ExistenceErrorMsg
import it.unibo.tuprolog.primitives.errors.HaltExceptionMsg
import it.unibo.tuprolog.primitives.errors.InitializationIssueMsg
import it.unibo.tuprolog.primitives.errors.InstantiationErrorMsg
import it.unibo.tuprolog.primitives.errors.LogicErrorMsg
import it.unibo.tuprolog.primitives.errors.MessageErrorMsg
import it.unibo.tuprolog.primitives.errors.MissingPredicateMsg
import it.unibo.tuprolog.primitives.errors.PermissionErrorMsg
import it.unibo.tuprolog.primitives.errors.RepresentationErrorMsg
import it.unibo.tuprolog.primitives.errors.ResolutionExceptionMsg
import it.unibo.tuprolog.primitives.errors.SyntaxErrorMsg
import it.unibo.tuprolog.primitives.errors.SystemErrorMsg
import it.unibo.tuprolog.primitives.errors.TimeOutExceptionMsg
import it.unibo.tuprolog.primitives.errors.TypeErrorMsg
import it.unibo.tuprolog.primitives.serialization.ParsingException
import it.unibo.tuprolog.solve.exception.HaltException
import it.unibo.tuprolog.solve.exception.LogicError
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.exception.TimeOutException
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.exception.error.DomainError
import it.unibo.tuprolog.solve.exception.error.EvaluationError
import it.unibo.tuprolog.solve.exception.error.ExistenceError
import it.unibo.tuprolog.solve.exception.error.InstantiationError
import it.unibo.tuprolog.solve.exception.error.MessageError
import it.unibo.tuprolog.solve.exception.error.PermissionError
import it.unibo.tuprolog.solve.exception.error.RepresentationError
import it.unibo.tuprolog.solve.exception.error.SyntaxError
import it.unibo.tuprolog.solve.exception.error.SystemError
import it.unibo.tuprolog.solve.exception.error.TypeError
import it.unibo.tuprolog.solve.exception.warning.InitializationIssue
import it.unibo.tuprolog.solve.exception.warning.MissingPredicate

fun ResolutionException.serialize(): ErrorMsg {
    val builder = ErrorMsg.newBuilder()
    this.message?.let {
        builder.setMessage(this.message)
    }
    this.cause?.let {
        builder.setCause(
            if (it is ResolutionException) {
                it.serialize()
            } else {
                ErrorMsg.newBuilder().setMessage(it.message).build()
            }
        )
    }
    return when (this) {
        is LogicError -> this.serialize(builder)
        is Warning -> this.serialize(builder)
        is HaltException ->
            builder.setHaltException(
                HaltExceptionMsg.newBuilder()
                    .setExitStatus(this.exitStatus)
            ).build()
        is TimeOutException ->
            builder.setTimeoutException(
                TimeOutExceptionMsg.newBuilder()
                    .setExceededDuration(this.exceededDuration)
            ).build()
        else -> builder.setResolutionException(ResolutionExceptionMsg.getDefaultInstance()).build()
    }
}

fun LogicError.serialize(builder: ErrorMsg.Builder): ErrorMsg {
    val logicErrorBuilder = LogicErrorMsg.newBuilder()
        .setType(this.type.serialize())
    this.extraData?.let { logicErrorBuilder.setExtraData(it.serialize()) }
    when (this) {
        is DomainError ->
            logicErrorBuilder.setDomainError(
                DomainErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedDomain(this.expectedDomain.name)
            )
        is EvaluationError ->
            logicErrorBuilder.setEvaluationError(
                EvaluationErrorMsg.newBuilder()
                    .setErrorType(this.errorType.name)
            )
        is ExistenceError ->
            logicErrorBuilder.setExistenceError(
                ExistenceErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedObject(this.expectedObject.name)
            )
        is InstantiationError ->
            logicErrorBuilder.setInstantiationError(
                InstantiationErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
            )
        is MessageError ->
            logicErrorBuilder.setMessageError(
                MessageErrorMsg.getDefaultInstance()
            )
        is PermissionError ->
            logicErrorBuilder.setPermissionError(
                PermissionErrorMsg.newBuilder()
                    .setOperation(this.operation.name)
                    .setPermission(this.permission.name)
                    .setCulprit(this.culprit.serialize())
            )
        is RepresentationError ->
            logicErrorBuilder.setRepresentationError(
                RepresentationErrorMsg.newBuilder()
                    .setLimit(this.limit.name)
            )
        is SyntaxError ->
            logicErrorBuilder.setSyntaxError(
                SyntaxErrorMsg.getDefaultInstance()
            )
        is SystemError ->
            logicErrorBuilder.setSystemError(
                SystemErrorMsg.getDefaultInstance()
            )
        is TypeError ->
            logicErrorBuilder.setTypeError(
                TypeErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedType(this.expectedType.name)
            )
        else -> throw ParsingException(this)
    }
    return builder.setLogicError(logicErrorBuilder).build()
}

fun Warning.serialize(builder: ErrorMsg.Builder): ErrorMsg =
    when (this) {
        is InitializationIssue ->
            builder.setInitializationIssue(
                InitializationIssueMsg.newBuilder()
                    .setGoal(this.goal.serialize())
            ).build()
        is MissingPredicate ->
            builder.setMissingPredicate(
                MissingPredicateMsg.newBuilder()
                    .setSignature(this.signature.serialize())
            ).build()
        else -> throw ParsingException(this)
    }
