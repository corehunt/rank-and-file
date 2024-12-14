"use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ChevronLeft, ChevronRight, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import {getNumberSuffix} from "@/utils/numberUtils";
import Link from "next/link";

interface BillDTO {
    billId: string;
    billNo: number;
    billTitle: string;
    introducedDt: string;
    latestActionDt: string;
    latestActionTxt: string;
    policyArea: string | null;
    congress: number;
    billType: string;
    originChamber: string;
    summaryTxt: string | null;
}

interface SponsoredLegislationDTO {
    sponLegId: string;
    sponsorType: string;
    bill: BillDTO;
}

interface PageDTO<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

interface SponsoredBillsProps {
    personId: string;
}

export default function SponsoredBills({ personId }: SponsoredBillsProps) {
    const [currentPage, setCurrentPage] = useState(1);
    const [data, setData] = useState<PageDTO<SponsoredLegislationDTO> | null>(
        null
    );
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchData() {
            setLoading(true);
            const fetchUrl = `/api/politician/${personId}/sponsored?page=${currentPage - 1}&size=10`;
            console.log("Fetching data from:", fetchUrl);
            try {
                const res = await fetch(fetchUrl, {
                    cache: "no-store",
                });
                if (res.ok) {
                    const jsonData: PageDTO<SponsoredLegislationDTO> = await res.json();
                    setData(jsonData);
                } else {
                    console.error("HTTP Error:", res.status);
                    setData(null);
                }
            } catch (error) {
                console.error("Fetch Error:", error);
                setData(null);
            }
            setLoading(false);
        }
        fetchData();
    }, [personId, currentPage]);

    function formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString(undefined, {
            year: "numeric",
            month: "long",
            day: "numeric",
        });
    }

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const renderPaginationButtons = () => {
        if (!data) return null;

        const totalPages = data.totalPages;
        const buttons = [];

        buttons.push(
            <Button
                key="prev"
                variant="outline"
                size="icon"
                disabled={currentPage === 1}
                onClick={() => handlePageChange(currentPage - 1)}
            >
                <ChevronLeft className="h-4 w-4" />
            </Button>
        );

        for (let i = 1; i <= totalPages; i++) {
            if (
                i === 1 ||
                i === totalPages ||
                (i >= currentPage - 1 && i <= currentPage + 1)
            ) {
                buttons.push(
                    <Button
                        key={i}
                        variant={currentPage === i ? "default" : "outline"}
                        onClick={() => handlePageChange(i)}
                        className="hidden sm:inline-flex"
                    >
                        {i}
                    </Button>
                );
            } else if (i === currentPage - 2 || i === currentPage + 2) {
                buttons.push(
                    <Button key={`dots-${i}`} variant="outline" disabled className="hidden sm:inline-flex">
                        ...
                    </Button>
                );
            }
        }

        buttons.push(
            <Button
                key="next"
                variant="outline"
                size="icon"
                disabled={currentPage === totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
            >
                <ChevronRight className="h-4 w-4" />
            </Button>
        );

        return buttons;
    };

    return (
        <div className="space-y-4">
            <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold">Sponsored Legislation</h2>
                {data && (
                    <Badge variant="secondary">{data.totalElements} Bills</Badge>
                )}
            </div>

            {loading ? (
                <p className="text-muted-foreground">Loading...</p>
            ) : data && data.content.length > 0 ? (
                <>
                    <div className="grid gap-4">
                        {data.content.map((item) => (
                            <Card key={item.sponLegId} className="overflow-hidden">
                                <CardContent className="p-6">
                                    <div className="space-y-4">
                                        <div className="flex flex-row items-start justify-between gap-4">
                                            {/* Title and Badges */}
                                            <div className="flex flex-col flex-grow min-w-0">
                                                <div className="flex items-center gap-2">
                                                    <FileText
                                                        className="h-4 w-4 text-muted-foreground shrink-0"/> {/* Consistent size */}
                                                    <Link
                                                        className="font-semibold text-base whitespace-nowrap overflow-hidden text-ellipsis"
                                                        title={item.bill.billTitle}
                                                        href={`/bills/${item.bill.billId}`}
                                                    >
                                                        {item.bill.billTitle}
                                                    </Link>
                                                </div>
                                                <div className="flex flex-wrap gap-2 mt-1"> {/* Add spacing */}
                                                    <Badge variant="outline">
                                                        {item.bill.billType} {item.bill.billNo}
                                                    </Badge>
                                                    <Badge variant="outline">
                                                        {item.bill.congress}{getNumberSuffix(item.bill.congress)} Congress
                                                    </Badge>
                                                </div>
                                            </div>
                                            <div className="text-sm text-muted-foreground shrink-0">
                                                Introduced {formatDate(item.bill.introducedDt)}
                                            </div>
                                        </div>

                                        <div className="space-y-2">
                                            <p className="text-sm font-medium">Latest Action</p>
                                            <p className="text-sm text-muted-foreground">
                                                {item.bill.latestActionTxt}
                                            </p>
                                            <p className="text-sm text-muted-foreground">
                                                {formatDate(item.bill.latestActionDt)}
                                            </p>
                                        </div>
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>

                    {/* Pagination Controls */}
                    {data.totalPages > 1 && (
                        <div className="flex justify-center gap-2 pt-4">
                            {renderPaginationButtons()}
                        </div>
                    )}

                    <div className="text-center text-sm text-muted-foreground">
                        Page {currentPage} of {data.totalPages}
                    </div>
                </>
            ) : (
                <p className="text-muted-foreground">
                    No sponsored legislation available.
                </p>
            )}
        </div>
    );
}