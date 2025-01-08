"use client";

import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Building2, Users } from "lucide-react";

interface ChamberPartyCount {
    count: number;
    chamber: string;
    party: string;
}

interface CongressData {
    democrats: number;
    republicans: number;
    independents: number;
    total: number;
}

export function CongressControl() {
    const [houseData, setHouseData] = useState<CongressData>({
        democrats: 0,
        republicans: 0,
        independents: 0,
        total: 0,
    });

    const [senateData, setSenateData] = useState<CongressData>({
        democrats: 0,
        republicans: 0,
        independents: 0,
        total: 0,
    });

    useEffect(() => {
        fetch("/api/congress")
            .then((res) => res.json())
            .then((rows: ChamberPartyCount[]) => {
                let houseDemocrats = 0;
                let houseRepublicans = 0;
                let houseIndependents = 0;

                let senateDemocrats = 0;
                let senateRepublicans = 0;
                let senateIndependents = 0;

                // Aggregate the counts by chamber and party
                rows.forEach((item) => {
                    if (item.chamber === "House of Representatives") {
                        if (item.party === "Democratic") {
                            houseDemocrats += item.count;
                        } else if (item.party === "Republican") {
                            houseRepublicans += item.count;
                        } else if (item.party === "Independent") {
                            houseIndependents += item.count;
                        }
                    } else if (item.chamber === "Senate") {
                        if (item.party === "Democratic") {
                            senateDemocrats += item.count;
                        } else if (item.party === "Republican") {
                            senateRepublicans += item.count;
                        } else if (item.party === "Independent") {
                            senateIndependents += item.count;
                        }
                    }
                });

                // Update House
                setHouseData({
                    democrats: houseDemocrats,
                    republicans: houseRepublicans,
                    independents: houseIndependents,
                    total: houseDemocrats + houseRepublicans + houseIndependents,
                });

                // Update Senate
                setSenateData({
                    democrats: senateDemocrats,
                    republicans: senateRepublicans,
                    independents: senateIndependents,
                    total: senateDemocrats + senateRepublicans + senateIndependents,
                });
            })
            .catch((error) => {
                console.error("Failed to fetch congress data:", error);
            });
    }, []);

    return (
        <Card>
            <CardHeader className="flex flex-row items-center gap-2">
                <Building2 className="h-5 w-5" />
                <h2 className="text-xl font-semibold">119th Congress</h2>
            </CardHeader>

            <CardContent>
                <div className="space-y-8">
                    {/* HOUSE */}
                    <div>
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="font-medium flex items-center gap-2">
                                <Users className="h-4 w-4" />
                                House of Representatives
                            </h3>
                            <span className="text-sm text-muted-foreground">
                {houseData.total} seats
              </span>
                        </div>
                        <div className="h-8 rounded-full overflow-hidden bg-muted/30 shadow-inner relative">
                            <div className="absolute inset-0 flex">
                                {/* Democrats (Left) */}
                                {houseData.democrats > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-primary/80 to-primary transition-all duration-500"
                                        style={{
                                            width: houseData.total
                                                ? `${(houseData.democrats / houseData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                                {/* Independents (Middle) */}
                                {houseData.independents > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-secondary/80 to-secondary transition-all duration-500"
                                        style={{
                                            width: houseData.total
                                                ? `${(houseData.independents / houseData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                                {/* Republicans (Right) */}
                                {houseData.republicans > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-destructive/80 to-destructive transition-all duration-500"
                                        style={{
                                            width: houseData.total
                                                ? `${(houseData.republicans / houseData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                            </div>
                        </div>
                        <div className="flex justify-between mt-3 text-sm">
                            {/* Democrats (Left) */}
                            <div className="flex items-center gap-2">
                                <div className="h-3 w-3 rounded-full bg-primary" />
                                <span>Democrats: {houseData.democrats}</span>
                            </div>
                            {/* Independents (Middle) */}
                            {houseData.independents > 0 && (
                                <div className="flex items-center gap-2">
                                    <div className="h-3 w-3 rounded-full bg-secondary" />
                                    <span>Independents: {houseData.independents}</span>
                                </div>
                            )}
                            {/* Republicans (Right) */}
                            <div className="flex items-center gap-2">
                                <div className="h-3 w-3 rounded-full bg-destructive" />
                                <span>Republicans: {houseData.republicans}</span>
                            </div>
                        </div>
                    </div>

                    {/* SENATE */}
                    <div>
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="font-medium flex items-center gap-2">
                                <Users className="h-4 w-4" />
                                Senate
                            </h3>
                            <span className="text-sm text-muted-foreground">
                {senateData.total} seats
              </span>
                        </div>
                        <div className="h-8 rounded-full overflow-hidden bg-muted/30 shadow-inner relative">
                            <div className="absolute inset-0 flex">
                                {/* Democrats (Left) */}
                                {senateData.democrats > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-primary/80 to-primary transition-all duration-500"
                                        style={{
                                            width: senateData.total
                                                ? `${(senateData.democrats / senateData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                                {/* Independents (Middle) */}
                                {senateData.independents > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-secondary/80 to-secondary transition-all duration-500"
                                        style={{
                                            width: senateData.total
                                                ? `${(senateData.independents / senateData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                                {/* Republicans (Right) */}
                                {senateData.republicans > 0 && (
                                    <div
                                        className="bg-gradient-to-r from-destructive/80 to-destructive transition-all duration-500"
                                        style={{
                                            width: senateData.total
                                                ? `${(senateData.republicans / senateData.total) * 100}%`
                                                : "0%",
                                        }}
                                    >
                                        <div className="h-full w-full opacity-20 bg-[radial-gradient(at_center_center,rgba(255,255,255,0.2)_0%,transparent_100%)]" />
                                    </div>
                                )}
                            </div>
                        </div>
                        <div className="flex justify-between mt-3 text-sm">
                            {/* Democrats (Left) */}
                            <div className="flex items-center gap-2">
                                <div className="h-3 w-3 rounded-full bg-primary" />
                                <span>Democrats: {senateData.democrats}</span>
                            </div>
                            {/* Independents (Middle) */}
                            {senateData.independents > 0 && (
                                <div className="flex items-center gap-2">
                                    <div className="h-3 w-3 rounded-full bg-secondary" />
                                    <span>Independents: {senateData.independents}</span>
                                </div>
                            )}
                            {/* Republicans (Right) */}
                            <div className="flex items-center gap-2">
                                <div className="h-3 w-3 rounded-full bg-destructive" />
                                <span>Republicans: {senateData.republicans}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
