"use client";

import { useState, useEffect } from "react";
import { Search, ChevronLeft, ChevronRight } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import PoliticianCard from "@/components/politicians/politician-card";
import PoliticianFilters from "@/components/politicians/politician-filters";
import {
  Sheet,
  SheetContent,
  SheetTrigger,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import backupImg from "@/app/assets/backup.png";

import { LeadershipSection } from "./components/leadership-section";

interface TermDTO {
  termId: number;
  chamber: string | null;
  congress: number;
  district: number | null;
  startYr: number;
  endYr: number | null;
  memberType: string | null;
  stateCd: string | null;
  stateNm: string | null;
}

interface PersonDTO {
  personId: string;
  firstName: string | null;
  midName: string | null;
  lastName: string | null;
  fullName: string | null;
  birthDate: string | null;
  deathDate: string | null;
  website: string | null;
  officeLocLine1: string | null;
  officeLocLine2: string | null;
  phoneNo: string | null;
  state: string | null;
  currentDistrict: number | null;
  currentMember: string | null;
  biography: string | null;
  email: string | null;
  imageUrl: string | null;
  imgAttribution: string | null;
  partyMembership: string | null;
  partyStartYr: number | null;
  termList: TermDTO[] | null;
}

interface LeadershipDTO {
  leadershipId: string;
  leadershipType: string;
  person: PersonSummaryDTO;
}

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

/**
 * Paged result structure from the backend.
 */
interface PageDTO<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export default function PoliticiansPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: [] as string[],
    parties: [] as string[],
    status: [] as string[],
  });

  // Paged results from the backend
  const [pagedResults, setPagedResults] = useState<PageDTO<PersonDTO> | null>(null);

  // State for controlling whether user is searching or viewing default
  const [isSearching, setIsSearching] = useState(false);

  // Pagination: 1-based
  const [currentPage, setCurrentPage] = useState(1);

  // Loading flag
  const [loading, setLoading] = useState(false);

  // State for fetched leadership
  const [leadership, setLeadership] = useState<LeadershipDTO[]>([]);
  const [loadingLeadership, setLoadingLeadership] = useState(true);
  const [leadershipError, setLeadershipError] = useState<string | null>(null);

  /** Fetch leadership on mount **/
  useEffect(() => {
    fetch("/api/politicians/leadership")
        .then((res) => {
          if (!res.ok) {
            throw new Error(`Error fetching leadership: ${res.statusText}`);
          }
          return res.json();
        })
        .then((data: LeadershipDTO[]) => {
          setLeadership(data);
          setLoadingLeadership(false);
        })
        .catch((error) => {
          console.error("Error fetching leadership:", error);
          setLeadershipError(error.message);
          setLoadingLeadership(false);
        });
  }, []);

  const handleFilterChange = (type: string, value: string | string[]) => {
    setFilters((prev) => ({
      ...prev,
      [type]: value,
    }));
  };

  /**
   * handleSearch: triggers search on user submit (Enter / Search button).
   */
  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();

    // Determine if filters are default
    const isDefaultFilters =
        filters.chamber.length === 0 &&
        filters.parties.length === 0 &&
        filters.status.length === 0;

    // If user typed nothing & filters are default -> show leadership
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
        q: searchQuery.trim(),
        page: String(page - 1), // backend expects zero-based
        size: "20",
      });

      // Add filters to query parameters
      if (filters.chamber.length > 0) {
        queryParams.append("chamber", filters.chamber.join(","));
      }
      if (filters.parties.length > 0) {
        queryParams.append("party", filters.parties.join(","));
      }
      if (filters.status.length > 0) {
        queryParams.append("status", filters.status.join(","));
      }

      const res = await fetch(`/api/politicians/[searchTerm]?${queryParams.toString()}`, {
        cache: "no-store",
      });
      if (!res.ok) {
        throw new Error("Failed to fetch search results");
      }

      const data: PageDTO<PersonDTO> = await res.json();
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  /**
   * Show leadership if no search has been done OR
   * if searched but no results & not currently loading
   */
  const showLeadership =
      !isSearching ||
      (isSearching && pagedResults && pagedResults.content.length === 0 && !loading);

  /**
   * Group leadership by House vs. Senate:
   * - If person.currentDistrict !== null => House
   * - If person.currentDistrict === null => Senate
   */
  const houseLeadership = leadership.filter(
      (item) => item.person.currentDistrict !== null
  );
  const senateLeadership = leadership.filter(
      (item) => item.person.currentDistrict === null
  );

  // Group by party
  const houseRepublican = houseLeadership.filter(
      (l) => l.person.partyMembership === "R"
  );
  const houseDemocratic = houseLeadership.filter(
      (l) => l.person.partyMembership === "D"
  );

  const senateRepublican = senateLeadership.filter(
      (l) => l.person.partyMembership === "R"
  );
  const senateDemocratic = senateLeadership.filter(
      (l) => l.person.partyMembership === "D"
  );

  /**
   * Helper to get chamber information
   */
  function getChamber(politician: PersonDTO): string {
    if (!politician.termList || politician.termList.length === 0) return "Unknown";
    const sortedTerms = [...politician.termList].sort((a, b) => b.startYr - a.startYr);
    const recentTerm = sortedTerms[0];
    return recentTerm.chamber || "Unknown";
  }

  /**
   * Helper to map party codes to full names
   */
  const partyMap: { [key: string]: string } = {
    R: "Republican",
    D: "Democratic",
    I: "Independent",
  };

  /**
   * Render Pagination Buttons
   */
  function renderPaginationButtons() {
    if (!pagedResults) return null;

    const { totalPages } = pagedResults;
    const buttons = [];

    // Prev Button
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

    // Page Numbers
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

    // Next Button
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

  return (
      <div className="min-h-screen bg-muted/30">
        {/* Page Header */}
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
            <h1 className="text-2xl sm:text-3xl font-bold mb-4">U.S. Politicians</h1>
            <p className="text-base sm:text-lg text-muted-foreground mb-6">
              Track voting records, sponsored bills, and financial connections of current and past Congress members.
            </p>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    type="search"
                    placeholder="Search by name, state, or party ..."
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
                <Button variant="outline" className="w-full">Filters</Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-[300px] sm:w-[400px]">
                <SheetHeader>
                  <SheetTitle>Filter Politicians</SheetTitle>
                </SheetHeader>
                <div className="mt-6">
                  <PoliticianFilters filters={filters} onFilterChange={handleFilterChange} />
                </div>
              </SheetContent>
            </Sheet>
          </div>

          {/* Desktop layout: Filters on the left, results on the right */}
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <aside className="hidden lg:block lg:col-span-1">
              <PoliticianFilters filters={filters} onFilterChange={handleFilterChange} />
            </aside>

            <main className="lg:col-span-3 space-y-8">
              {/* Politicians Results */}
              {isSearching && (
                  <>
                    {loading ? (
                        <div className="text-center text-muted-foreground py-8">Loading...</div>
                    ) : pagedResults && pagedResults.content.length === 0 ? (
                        <div className="text-center text-muted-foreground py-8">
                          No politicians found matching your criteria
                        </div>
                    ) : pagedResults && pagedResults.content.length > 0 ? (
                        <>
                          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
                            {pagedResults.content.map((politician) => {
                              const chamber = getChamber(politician);
                              const isSenator = chamber === "Senate";
                              let districtDisplay = "";
                              if (!isSenator) {
                                if (politician.currentDistrict !== null) {
                                  districtDisplay = `District ${politician.currentDistrict}`;
                                } else {
                                  districtDisplay = "District Unknown";
                                }
                              }

                              const partyName =
                                  partyMap[politician.partyMembership || ""] || "Unknown";

                              const status =
                                  politician.currentMember === "Yes"
                                      ? "Incumbent"
                                      : "Former Member";

                              return (
                                  <PoliticianCard
                                      key={politician.personId}
                                      name={politician.fullName || "Unknown"}
                                      state={politician.state || "Unknown"}
                                      party={partyName}
                                      district={districtDisplay}
                                      imageUrl={politician.imageUrl || backupImg}
                                      status={status}
                                      chamber={chamber}
                                      personId={politician.personId}
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
                          {pagedResults.totalPages > 1 && (
                              <div className="text-center text-sm text-muted-foreground mt-2">
                                Page {currentPage} of {pagedResults.totalPages}
                              </div>
                          )}
                        </>
                    ) : null}
                  </>
              )}

              {/* Leadership Sections - Only show if no search has been done OR no results */}
              {showLeadership && (
                  <>
                    {/* If leadership is still loading, show a loader or error */}
                    {loadingLeadership && (
                        <div className="text-center text-muted-foreground py-8">
                          Loading leadership...
                        </div>
                    )}
                    {leadershipError && (
                        <div className="text-center text-destructive py-8">
                          Error fetching leadership: {leadershipError}
                        </div>
                    )}

                    {!loadingLeadership && !leadershipError && leadership.length > 0 && (
                        <>
                          <div className="bg-card rounded-lg border p-6">
                            <h2 className="text-2xl font-bold mb-6">House Leadership</h2>
                            <div className="grid gap-8">
                              <div className="bg-muted/50 rounded-lg p-6">
                                <LeadershipSection
                                    title="Republican Leadership"
                                    leaders={houseRepublican}
                                />
                              </div>
                              <div className="bg-muted/50 rounded-lg p-6">
                                <LeadershipSection
                                    title="Democratic Leadership"
                                    leaders={houseDemocratic}
                                />
                              </div>
                            </div>
                          </div>

                          <div className="bg-card rounded-lg border p-6">
                            <h2 className="text-2xl font-bold mb-6">Senate Leadership</h2>
                            <div className="grid gap-8">
                              <div className="bg-muted/50 rounded-lg p-6">
                                <LeadershipSection
                                    title="Republican Leadership"
                                    leaders={senateRepublican}
                                />
                              </div>
                              <div className="bg-muted/50 rounded-lg p-6">
                                <LeadershipSection
                                    title="Democratic Leadership"
                                    leaders={senateDemocratic}
                                />
                              </div>
                            </div>
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