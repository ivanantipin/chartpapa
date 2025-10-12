"""
Trading Inefficiency Monitor - Detects divergences and arbitrage opportunities
"""

import logging
import time
from typing import List, Dict, Any, Optional, Callable
from datetime import datetime
from dataclasses import dataclass, asdict
import json

from .client import FinamClient
from .market_data import MarketDataService
from .config import DIVERGENCE_THRESHOLD_PERCENT, MONITORING_INTERVAL_SECONDS

logger = logging.getLogger(__name__)


@dataclass
class DivergenceAlert:
    """Data class for divergence alerts"""
    timestamp: str
    base_symbol: str
    futures_symbol: str
    base_price: float
    futures_price: float
    spread: float
    spread_percent: float
    alert_type: str
    severity: str

    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary"""
        return asdict(self)

    def to_json(self) -> str:
        """Convert to JSON string"""
        return json.dumps(self.to_dict(), indent=2)


class DivergenceMonitor:
    """
    Monitor for detecting price divergences between base assets and futures

    Monitors specified asset pairs and triggers alerts when divergence
    exceeds configured thresholds.
    """

    def __init__(
        self,
        client: FinamClient,
        threshold_percent: Optional[float] = None,
        monitoring_interval: Optional[int] = None
    ):
        """
        Initialize divergence monitor

        Args:
            client: Authenticated FinamClient instance
            threshold_percent: Divergence threshold in percent (default from config)
            monitoring_interval: Monitoring interval in seconds (default from config)
        """
        self.client = client
        self.market_data = MarketDataService(client)
        self.threshold_percent = threshold_percent or DIVERGENCE_THRESHOLD_PERCENT
        self.monitoring_interval = monitoring_interval or MONITORING_INTERVAL_SECONDS

        self.monitored_pairs: List[Dict[str, str]] = []
        self.alert_callbacks: List[Callable[[DivergenceAlert], None]] = []
        self.is_monitoring = False
        self.alert_history: List[DivergenceAlert] = []

        logger.info(
            f"Divergence monitor initialized with threshold={self.threshold_percent}%, "
            f"interval={self.monitoring_interval}s"
        )

    def add_monitored_pair(self, base_symbol: str, futures_symbol: str) -> None:
        """
        Add an asset pair to monitor

        Args:
            base_symbol: Symbol of the base asset (e.g., "SBER@TQBR")
            futures_symbol: Symbol of the futures contract (e.g., "SRH5@RFUD")
        """
        pair = {
            "base_symbol": base_symbol,
            "futures_symbol": futures_symbol
        }

        if pair not in self.monitored_pairs:
            self.monitored_pairs.append(pair)
            logger.info(f"Added monitoring pair: {base_symbol} <-> {futures_symbol}")
        else:
            logger.warning(f"Pair already monitored: {base_symbol} <-> {futures_symbol}")

    def remove_monitored_pair(self, base_symbol: str, futures_symbol: str) -> None:
        """
        Remove an asset pair from monitoring

        Args:
            base_symbol: Symbol of the base asset
            futures_symbol: Symbol of the futures contract
        """
        pair = {
            "base_symbol": base_symbol,
            "futures_symbol": futures_symbol
        }

        if pair in self.monitored_pairs:
            self.monitored_pairs.remove(pair)
            logger.info(f"Removed monitoring pair: {base_symbol} <-> {futures_symbol}")
        else:
            logger.warning(f"Pair not found in monitored list: {base_symbol} <-> {futures_symbol}")

    def add_alert_callback(self, callback: Callable[[DivergenceAlert], None]) -> None:
        """
        Add a callback function to be called when an alert is triggered

        Args:
            callback: Function that takes a DivergenceAlert as parameter
        """
        self.alert_callbacks.append(callback)
        logger.info(f"Added alert callback: {callback.__name__}")

    def check_divergence(self, base_symbol: str, futures_symbol: str) -> Optional[DivergenceAlert]:
        """
        Check for divergence between a base asset and futures

        Args:
            base_symbol: Symbol of the base asset
            futures_symbol: Symbol of the futures contract

        Returns:
            DivergenceAlert if divergence detected, None otherwise
        """
        try:
            comparison = self.market_data.get_price_comparison(base_symbol, futures_symbol)

            spread_percent = comparison.get("spread_percent")

            if spread_percent is None:
                logger.warning(f"Could not calculate spread for {base_symbol} <-> {futures_symbol}")
                return None

            # Check if divergence exceeds threshold
            abs_spread_percent = abs(spread_percent)

            if abs_spread_percent >= self.threshold_percent:
                # Determine severity based on spread magnitude
                if abs_spread_percent >= self.threshold_percent * 3:
                    severity = "critical"
                elif abs_spread_percent >= self.threshold_percent * 2:
                    severity = "high"
                else:
                    severity = "medium"

                # Determine alert type
                if spread_percent > 0:
                    alert_type = "futures_premium"
                else:
                    alert_type = "futures_discount"

                alert = DivergenceAlert(
                    timestamp=comparison["timestamp"],
                    base_symbol=base_symbol,
                    futures_symbol=futures_symbol,
                    base_price=comparison["base_price"],
                    futures_price=comparison["futures_price"],
                    spread=comparison["spread"],
                    spread_percent=spread_percent,
                    alert_type=alert_type,
                    severity=severity
                )

                logger.warning(
                    f"DIVERGENCE DETECTED [{severity.upper()}]: {base_symbol} vs {futures_symbol} "
                    f"- Spread: {spread_percent:.2f}%"
                )

                return alert

            else:
                logger.debug(
                    f"No significant divergence: {base_symbol} vs {futures_symbol} "
                    f"- Spread: {spread_percent:.2f}%"
                )
                return None

        except Exception as e:
            logger.error(f"Error checking divergence for {base_symbol} <-> {futures_symbol}: {e}")
            return None

    def _trigger_alert(self, alert: DivergenceAlert) -> None:
        """
        Trigger an alert by calling all registered callbacks

        Args:
            alert: DivergenceAlert to trigger
        """
        self.alert_history.append(alert)

        for callback in self.alert_callbacks:
            try:
                callback(alert)
            except Exception as e:
                logger.error(f"Error in alert callback {callback.__name__}: {e}")

    def scan_all_pairs(self) -> List[DivergenceAlert]:
        """
        Scan all monitored pairs for divergences

        Returns:
            List of DivergenceAlerts detected
        """
        alerts = []

        logger.info(f"Scanning {len(self.monitored_pairs)} pairs for divergences...")

        for pair in self.monitored_pairs:
            base_symbol = pair["base_symbol"]
            futures_symbol = pair["futures_symbol"]

            alert = self.check_divergence(base_symbol, futures_symbol)

            if alert:
                alerts.append(alert)
                self._trigger_alert(alert)

        if alerts:
            logger.info(f"Found {len(alerts)} divergences")
        else:
            logger.info("No divergences detected")

        return alerts

    def start_monitoring(self) -> None:
        """
        Start continuous monitoring of all pairs

        This is a blocking call that runs indefinitely.
        Use stop_monitoring() to stop.
        """
        if self.is_monitoring:
            logger.warning("Monitoring is already running")
            return

        if not self.monitored_pairs:
            logger.warning("No pairs configured for monitoring")
            return

        self.is_monitoring = True
        logger.info("Starting continuous monitoring...")

        try:
            while self.is_monitoring:
                scan_start = time.time()

                self.scan_all_pairs()

                scan_duration = time.time() - scan_start
                sleep_time = max(0, self.monitoring_interval - scan_duration)

                if sleep_time > 0:
                    logger.debug(f"Sleeping for {sleep_time:.1f}s until next scan")
                    time.sleep(sleep_time)
                else:
                    logger.warning(
                        f"Scan took {scan_duration:.1f}s, longer than interval "
                        f"{self.monitoring_interval}s"
                    )

        except KeyboardInterrupt:
            logger.info("Monitoring stopped by user")
        except Exception as e:
            logger.error(f"Error in monitoring loop: {e}")
            raise
        finally:
            self.is_monitoring = False
            logger.info("Monitoring stopped")

    def stop_monitoring(self) -> None:
        """Stop continuous monitoring"""
        if self.is_monitoring:
            logger.info("Stopping monitoring...")
            self.is_monitoring = False
        else:
            logger.warning("Monitoring is not running")

    def get_alert_history(
        self,
        limit: Optional[int] = None,
        severity: Optional[str] = None
    ) -> List[DivergenceAlert]:
        """
        Get alert history

        Args:
            limit: Maximum number of alerts to return (most recent first)
            severity: Filter by severity (critical, high, medium)

        Returns:
            List of DivergenceAlerts
        """
        alerts = self.alert_history

        if severity:
            alerts = [a for a in alerts if a.severity == severity]

        # Return most recent first
        alerts = sorted(alerts, key=lambda x: x.timestamp, reverse=True)

        if limit:
            alerts = alerts[:limit]

        return alerts

    def get_statistics(self) -> Dict[str, Any]:
        """
        Get monitoring statistics

        Returns:
            Dictionary with monitoring statistics
        """
        total_alerts = len(self.alert_history)

        severity_counts = {
            "critical": 0,
            "high": 0,
            "medium": 0
        }

        for alert in self.alert_history:
            severity_counts[alert.severity] = severity_counts.get(alert.severity, 0) + 1

        return {
            "total_monitored_pairs": len(self.monitored_pairs),
            "is_monitoring": self.is_monitoring,
            "threshold_percent": self.threshold_percent,
            "monitoring_interval": self.monitoring_interval,
            "total_alerts": total_alerts,
            "alerts_by_severity": severity_counts,
            "monitored_pairs": self.monitored_pairs
        }


# Default alert callback that prints to console
def console_alert_callback(alert: DivergenceAlert) -> None:
    """
    Default callback that prints alerts to console

    Args:
        alert: DivergenceAlert to print
    """
    print("\n" + "=" * 80)
    print(f"DIVERGENCE ALERT [{alert.severity.upper()}]")
    print("=" * 80)
    print(f"Time:           {alert.timestamp}")
    print(f"Base Asset:     {alert.base_symbol} @ {alert.base_price:.2f}")
    print(f"Futures:        {alert.futures_symbol} @ {alert.futures_price:.2f}")
    print(f"Spread:         {alert.spread:.2f} ({alert.spread_percent:.2f}%)")
    print(f"Type:           {alert.alert_type}")
    print("=" * 80 + "\n")
