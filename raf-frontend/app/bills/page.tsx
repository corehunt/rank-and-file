"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
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

export default function BillsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: [] as string[],
    party: [] as string[],
    congress: [] as string[],
  });

  // Store recent bills (fetched on mount)
  const [recentBills, setRecentBills] = useState<BillDTO[]>([]);

  // Store search results (populated only when user submits)
  const [searchResults, setSearchResults] = useState<BillDTO[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  /**
   * 1) Fetch “recent bills” on mount.
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

  /**
   * handleFilterChange
   * If you want filter changes to immediately re-trigger searching,
   * call `handleSearch()` inside here. But for now,
   * we only search on form submit, so do NOT auto-fetch.
   */
  function handleFilterChange(type: string, value: string | string[]) {
    setFilters((prev) => ({
      ...prev,
      [type]: value,
    }));
  }

  /**
   * handleSearch
   * Triggers the search manually on form submission.
   */
  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();

    // Check if the user typed anything or changed filters
    const isDefaultFilters =
        filters.chamber.length === 0 &&
        filters.party.length === 0 &&
        filters.congress.length === 0;

    // If no search query & default filters → show recent bills
    if (!searchQuery.trim() && isDefaultFilters) {
      setSearchResults([]);
      setIsSearching(false);
      return;
    }

    // Otherwise, actually do the search
    try {
      setIsSearching(true);

      const queryParams = new URLSearchParams({
        q: searchQuery,
        chamber: filters.chamber.join(","),
        party: filters.party.join(","),
        congress: filters.congress.join(","),
      });

      const res = await fetch(`/api/bills?${queryParams}`, { cache: "no-store" });
      if (!res.ok) {
        throw new Error("Failed to fetch search results");
      }

      const data: BillDTO[] = await res.json();
      setSearchResults(data);
    } catch (error) {
      console.error("Error fetching search results:", error);
      setSearchResults([]);
    }
  }

  /**
   * Helper function to get main sponsor.
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

  /**
   * Decide what to display
   */
  const showSearchResults = isSearching && searchResults.length > 0;
  const showNoResults = isSearching && searchResults.length === 0;
  const showRecentBills = !isSearching; // user hasn't triggered search yet

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
              {/* Show Recent Bills if not searching */}
              {showRecentBills && recentBills.length > 0 && (
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

              {/* Search Results */}
              {showSearchResults && (
                  <div className="grid gap-6">
                    {searchResults.map((bill) => {
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
              )}

              {/* No Results */}
              {showNoResults && (
                  <div className="text-center text-muted-foreground py-8">
                    No bills found matching your criteria
                  </div>
              )}
            </main>
          </div>
        </div>
      </div>
  );
}