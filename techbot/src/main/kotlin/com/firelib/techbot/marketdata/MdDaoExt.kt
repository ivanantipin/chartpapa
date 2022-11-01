package com.firelib.techbot.marketdata

import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.core.store.MdDao
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object MdDaoExt{
    suspend fun MdDao.truncateSuspend(instrId: InstrId){
        suspendCoroutine { cont ->
            this.truncate(instrId).handle { _, thr ->
                if (thr != null) {
                    cont.resumeWithException(thr)
                } else {
                    cont.resumeWith(Result.success("OK"))
                }
            }
        }
    }

    suspend fun MdDao.insertOhlcSuspend(ohlcs : List<Ohlc>, instrId : InstrId){
        suspendCoroutine { cont ->
            this.insertOhlc(ohlcs, instrId).handle { _, thr ->
                if (thr != null) {
                    cont.resumeWithException(thr)
                } else {
                    cont.resumeWith(Result.success("OK"))
                }
            }
        }
    }

}