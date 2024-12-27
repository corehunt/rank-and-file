"use client";

import { useState, useEffect } from "react";
import { Search, ChevronLeft, ChevronRight } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import BillCard from "@/components/bills/bill-card";
import BillFilters from "@/components/bills/bill-filters";
import {
  Sheet,
  SheetContent,
  SheetTrigger,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";

interface PersonSummaryDTO {
  personId: string;
  firstName: string | null;
  midName: string | null;
  lastName: string | null;
  fullName: string | null;
  state: string | null;
  currentDistrict: number | null;
  imageUrl: string | null;
  partyMembership: string | null;
}

interface SponsoredLegPersonDTO {
  sponLegId: string;
  sponsorType: string | null;
  person: PersonSummaryDTO | null;
}

interface BillDTO {
  billId: string;
  billNo: number | null;
  billTitle: string | null;
  introducedDt: string | null;
  latestActionDt: string | null;
  latestActionTxt: string | null;
  policyArea: string | null;
  congress: number | null;
  billType: string | null;
  originChamber: string | null;
  summaryTxt: string | null;
  legislativeSubjects: string | null;
  sponsorships: SponsoredLegPersonDTO[] | null;
}

/**
 * Paged result structure from the backend.
 * Adjust if your fields differ.
 */
interface PageDTO<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // zero-based page index
}

/* ---------------------- Main Component ---------------------- */
export default function BillsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: [] as string[],
    party: [] as string[],
    congress: [] as string[],
  });

  // "recentBills" for when user hasn't triggered search
  const [recentBills, setRecentBills] = useState<BillDTO[]>([]);

  // Page-based results from the backend
  const [pagedResults, setPagedResults] = useState<PageDTO<BillDTO> | null>(null);

  // State for controlling whether user is searching or viewing recent
  const [isSearching, setIsSearching] = useState(false);

  // Pagination: 1-based
  const [currentPage, setCurrentPage] = useState(1);

  // Loading flag
  const [loading, setLoading] = useState(false);

  /**
   * 1) Fetch recent bills on mount
   */
  useEffect(() => {
    async function fetchRecentBills() {
      try {
        const res = await fetch("/api/bills/recent", { cache: "no-store" });
        if (!res.ok) {
          throw new Error("Failed to fetch recent bills");
        }
        const data: BillDTO[] = await res.json();
        setRecentBills(data);
      } catch (error) {
        console.error("Error fetching recent bills:", error);
      }
    }
    fetchRecentBills();
  }, []);

  function handleFilterChange(type: string, value: string | string[]) {
    setFilters((prev) => ({
      ...prev,
      [type]: value,
    }));
  }

  /**
   * handleSearch: triggers search on user submit (Enter / Search button).
   */
  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();

    // If user typed nothing & filters are default -> show recent
    const isDefaultFilters =
        filters.chamber.length === 0 &&
        filters.party.length === 0 &&
        filters.congress.length === 0;

    if (!searchQuery.trim() && isDefaultFilters) {
      setIsSearching(false);
      setPagedResults(null);
      setCurrentPage(1);
      return;
    }

    // Otherwise, do an actual search on page 1
    setCurrentPage(1);
    await doSearch(1); // fetch page 1
  }

  /**
   * doSearch: fetches from the backend with pagination
   * - page is 1-based
   */
  async function doSearch(page: number) {
    try {
      setIsSearching(true);
      setLoading(true);

      const queryParams = new URLSearchParams({
        q: searchQuery,
        chamber: filters.chamber.join(","),
        party: filters.party.join(","),
        congress: filters.congress.join(","),
        page: String(page - 1), // backend expects zero-based
        size: "10",
      });

      const res = await fetch(`/api/bills?${queryParams}`, { cache: "no-store" });
      if (!res.ok) {
        throw new Error("Failed to fetch search results");
      }

      const data: PageDTO<BillDTO> = await res.json();
      setPagedResults(data);
    } catch (error) {
      console.error("Error fetching search results:", error);
      setPagedResults(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (isSearching) {
      doSearch(currentPage);
    }
  }, [currentPage]);

  /**
   * getMainSponsorName helper
   */
  function getMainSponsorName(bill: BillDTO): { name: string; party: string; state: string } | null {
    if (!bill.sponsorships || bill.sponsorships.length === 0) return null;
    const mainSponsor = bill.sponsorships.find(
        (s) => s.sponsorType && s.sponsorType.toLowerCase() === "sponsor"
    );
    if (!mainSponsor || !mainSponsor.person) return null;
    return {
      name: mainSponsor.person.fullName || "Unknown Sponsor",
      party: mainSponsor.person.partyMembership || "Unknown",
      state: mainSponsor.person.state || "??",
    };
  }

  function renderPaginationButtons() {
    if (!pagedResults) return null;

    const { totalPages } = pagedResults;
    const buttons = [];

    // Prev
    buttons.push(
        <Button
            key="prev"
            variant="outline"
            size="icon"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(currentPage - 1)}
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
    );

    // Page #s
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
                onClick={() => setCurrentPage(i)}
                className="hidden sm:inline-flex"
            >
              {i}
            </Button>
        );
      } else if (i === currentPage - 2 || i === currentPage + 2) {
        buttons.push(
            <Button
                key={`dots-${i}`}
                variant="outline"
                disabled
                className="hidden sm:inline-flex"
            >
              ...
            </Button>
        );
      }
    }

    // Next
    buttons.push(
        <Button
            key="next"
            variant="outline"
            size="icon"
            disabled={pagedResults && currentPage === totalPages}
            onClick={() => setCurrentPage(currentPage + 1)}
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
    );

    return buttons;
  }

  const showingSearchResults = isSearching && pagedResults !== null;

  const noResults =
      isSearching && !loading && pagedResults && pagedResults.content.length === 0;

  return (
      <div className="min-h-screen bg-muted/30">
        {/* Page Header */}
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
            <h1 className="text-2xl sm:text-3xl font-bold mb-4">Legislative Bills</h1>
            <p className="text-base sm:text-lg text-muted-foreground mb-6">
              Track and analyze current and historical legislation in Congress.
            </p>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    type="search"
                    placeholder="Search bills by keyword, number, or sponsor..."
                    className="pl-10"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <Button type="submit">Search</Button>
            </form>
          </div>
        </div>

        {/* Main Content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Mobile Filters */}
          <div className="lg:hidden mb-4">
            <Sheet>
              <SheetTrigger asChild>
                <Button variant="outline" className="w-full">
                  Filters
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-[300px] sm:w-[400px]">
                <SheetHeader>
                  <SheetTitle>Filter Bills</SheetTitle>
                </SheetHeader>
                <div className="mt-6">
                  <BillFilters filters={filters} onFilterChange={handleFilterChange} />
                </div>
              </SheetContent>
            </Sheet>
          </div>

          {/* Desktop layout: Filters on the left, results on the right */}
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <aside className="hidden lg:block lg:col-span-1">
              <BillFilters filters={filters} onFilterChange={handleFilterChange} />
            </aside>

            <main className="lg:col-span-3">
              {/* If not searching, show recent bills */}
              {!showingSearchResults && recentBills.length > 0 && (
                  <section className="mb-8">
                    <h2 className="text-xl font-semibold mb-4">Recent Bills</h2>
                    <div className="grid gap-6">
                      {recentBills.map((bill) => {
                        const sponsorInfo = getMainSponsorName(bill);
                        return (
                            <BillCard
                                key={bill.billId}
                                billId={bill.billId}
                                displayTitle={
                                  bill.billType && bill.billNo
                                      ? `${bill.billType} ${bill.billNo} - ${bill.billTitle}`
                                      : bill.billTitle || "Untitled Bill"
                                }
                                sponsorName={sponsorInfo?.name}
                                sponsorParty={sponsorInfo?.party}
                                sponsorState={sponsorInfo?.state}
                                introducedDate={
                                  bill.introducedDt
                                      ? new Date(bill.introducedDt).toLocaleDateString()
                                      : undefined
                                }
                                summary={bill.summaryTxt || undefined}
                            />
                        );
                      })}
                    </div>
                  </section>
              )}

              {/* If searching, show loading or results */}
              {isSearching && (
                  <>
                    {/* Loading indicator */}
                    {loading && (
                        <div className="text-center text-muted-foreground py-4">
                          Loading...
                        </div>
                    )}

                    {/* No Results */}
                    {noResults && (
                        <div className="text-center text-muted-foreground py-8">
                          No bills found matching your criteria
                        </div>
                    )}

                    {/* Show Paged Results */}
                    {!loading && pagedResults && pagedResults.content.length > 0 && (
                        <>
                          <div className="grid gap-6">
                            {pagedResults.content.map((bill) => {
                              const sponsorInfo = getMainSponsorName(bill);
                              return (
                                  <BillCard
                                      key={bill.billId}
                                      billId={bill.billId}
                                      displayTitle={
                                        bill.billType && bill.billNo
                                            ? `${bill.billType} ${bill.billNo} - ${bill.billTitle}`
                                            : bill.billTitle || "Untitled Bill"
                                      }
                                      sponsorName={sponsorInfo?.name}
                                      sponsorParty={sponsorInfo?.party}
                                      sponsorState={sponsorInfo?.state}
                                      introducedDate={
                                        bill.introducedDt
                                            ? new Date(bill.introducedDt).toLocaleDateString()
                                            : undefined
                                      }
                                      summary={bill.summaryTxt || undefined}
                                  />
                              );
                            })}
                          </div>

                          {/* Pagination */}
                          {pagedResults.totalPages > 1 && (
                              <div className="flex justify-center gap-2 pt-4">
                                {renderPaginationButtons()}
                              </div>
                          )}
                          <div className="text-center text-sm text-muted-foreground mt-2">
                            Page {currentPage} of {pagedResults.totalPages}
                          </div>
                        </>
                    )}
                  </>
              )}
            </main>
          </div>
        </div>
      </div>
  );
}