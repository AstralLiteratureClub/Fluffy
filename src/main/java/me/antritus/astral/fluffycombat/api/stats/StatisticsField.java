package me.antritus.astral.fluffycombat.api.stats;

import java.util.Objects;

/**
 * Required size: 5.<p>
 * For example, good values would be as following:<p>
 *  - Kills: 1 = bad, 10 = better, 20 = even better, 40 = good, 50 = best<p>
 *      values: [1, 10, 20, 40, 50]<p>
 *      valueColors: ["red", "orange", "yellow", "green", "dark_green"]<p>
 *      valueType: HIGHER_BETTER<p>
 *      Displays Values as:<br>
 *      Value:<br>
 *              0: BAD<br>
 *              1: BAD<br>
 *              3: BETTER<br>
 *              10: BETTER<br>
 *              15: EVEN BETTER<br>
 *              20: EVEN BETTER<br>
 *              40: GOOD<br>
 *              50: BEST<br>
 *  - Deaths:50 = worst, 30 = even worse, 20 = worse, 10 = bad, 2 = best<p>
 *      values: [2, 10, 20, 30, 50]<p>
 *      valueColors: ["dark_green", "green", "yellow", "gold", "red"]<p>
 *      valueType: LOWER_BETTER<p>
 *      Displays Values as:<br>
 *      Value:<br>
 *              0: BEST<br>
 *              1: BEST<br>
 *              3: BAD<br>
 *              10: BAD<br>
 *              15: WORSE<br>
 *              20: WORSE<br>
 *              25: EVEN WORSE<br>
 *              30: WORST<br>
 *  </p>
 */
public @interface StatisticsField {
	String fieldName();
	String category();
}
