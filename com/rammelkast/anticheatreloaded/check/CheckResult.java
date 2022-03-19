/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2022 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.rammelkast.anticheatreloaded.check;

public class CheckResult {

    public enum Result {
        PASSED, FAILED
    }

    private Result result;
    private String subCheck;
    private String message;
    private int data;

    public CheckResult(Result result, String message, int data) {
        this(result, message);
        this.data = data;
    }

    public CheckResult(Result result, String subcheck, String message) {
        this(result);
        this.subCheck = subcheck;
        this.message = message;
    }

    public CheckResult(Result result, String message) {
        this(result);
        this.message = message;
    }

    public CheckResult(Result result) {
        this.result = result;
    }

    public boolean failed() {
        return result == Result.FAILED;
    }
    
    public String getSubCheck() {
    	return subCheck;
    }

    public String getMessage() {
        return message;
    }
    
    public Result getResult() {
        return result;
    }

    public int getData() {
        return data;
    }

}
