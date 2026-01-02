//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2025 emilyy-dev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.interfaces.storage;

/**
 * Interface for storage-specific data migration systems.
 * Each storage implementation should provide its own upgrader.
 */
public interface StorageDataUpgrader {

    /**
     * Gets the current version of the prisoner data schema.
     *
     * @return The current version
     */
    int getCurrentPrisonerVersion();

    /**
     * Gets the current version of the jails data schema.
     *
     * @return The current version
     */
    int getCurrentJailsVersion();

    /**
     * Upgrades prisoner data from one version to another.
     *
     * @param data        The data to upgrade (format depends on storage implementation)
     * @param fromVersion The version to upgrade from
     * @param toVersion   The version to upgrade to
     * @throws Exception If the upgrade fails
     */
    void upgradePrisonerData(Object data, int fromVersion, int toVersion) throws Exception;

    /**
     * Upgrades jails data from one version to another.
     *
     * @param data        The data to upgrade (format depends on storage implementation)
     * @param fromVersion The version to upgrade from
     * @param toVersion   The version to upgrade to
     * @throws Exception If the upgrade fails
     */
    void upgradeJailsData(Object data, int fromVersion, int toVersion) throws Exception;
}