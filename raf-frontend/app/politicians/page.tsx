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
  currentLeader?: string;
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

const atLargeStates = [
  "Alaska",
  "Wyoming",
  "Montana",
  "North Dakota",
  "South Dakota",
  "Vermont",
  "Delaware",
  "Virgin Islands",
  "Puerto Rico",
  "District of Columbia",
  "Guam",
  "American Samoa",
  "Northern Mariana Islands",
];

function getDistrictDisplay(term: TermDTO | undefined): string {
  if (!term || term.chamber === "Senate") return "";
  if (atLargeStates.includes(term.stateNm || "")) return "At Large";
  return term.district ? `District ${term.district}` : "District Unknown";
}

function getMostRecentTerm(termList: TermDTO[] | null): TermDTO | undefined {
  if (!termList || termList.length === 0) return undefined;
  return termList.reduce((prev, current) =>
      current.congress > prev.congress ? current : prev
  );
}

export default function PoliticiansPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState<{
    chamber: string;
    parties: string[];
    status: string[];
  }>({
    chamber: "all",
    parties: [],
    status: [],
  });

  // This holds whatever the backend returned, unfiltered.
  const [pagedResults, setPagedResults] = useState<PageDTO<PersonDTO> | null>(null);

  // If isSearching = false => leadership mode, else => show search results
  const [isSearching, setIsSearching] = useState(false);

  // Pagination tracking from the server’s response
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(false);

  // Leadership data from /api/politicians/leadership
  const [leadership, setLeadership] = useState<LeadershipDTO[]>([]);
  const [loadingLeadership, setLoadingLeadership] = useState(true);
  const [leadershipError, setLeadershipError] = useState<string | null>(null);

  // Fetch leadership on mount
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

  // Called when the user changes a filter in PoliticianFilters
  const handleFilterChange = (
      type: "chamber" | "parties" | "status",
      value: string | string[]
  ) => {
    setFilters((prev) => ({
      ...prev,
      [type]: value,
    }));
  };

  // Called when user submits the search form
  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();

    // If empty search + default filters => leadership mode
    const isDefaultFilters =
        filters.chamber === "all" &&
        filters.parties.length === 0 &&
        filters.status.length === 0;

    if (!searchQuery.trim() && isDefaultFilters) {
      setIsSearching(false);
      setPagedResults(null);
      setCurrentPage(1);
      return;
    }

    // Otherwise, do a one-time fetch from the backend
    setCurrentPage(1);
    await doSearch(1);
  }

  // Actually fetch data from /api/politicians/[searchTerm]?...
  async function doSearch(page: number) {
    try {
      setIsSearching(true);
      setLoading(true);

      const queryParams = new URLSearchParams({
        q: searchQuery.trim(),
        page: String(page - 1), // zero-based
        size: "20",
      });

      const dynamicPart = searchQuery.trim() || "all";

      const endpoint = `/api/politicians/${encodeURIComponent(dynamicPart)}?${queryParams.toString()}`;

      const res = await fetch(endpoint, {
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

  // Re-fetch if user changes pages, but do not add filters as params
  useEffect(() => {
    if (isSearching) {
      doSearch(currentPage);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  // Whether to show leadership sections
  const showLeadership =
      !isSearching ||
      (isSearching && pagedResults && pagedResults.content.length === 0 && !loading);

  // A local function to get the "chamber" for a politician
  function localChamber(p: PersonDTO): string {
    const term = getMostRecentTerm(p.termList);
    if (!term) return "Unknown";
    return term.chamber || "Unknown";
  }

  // Map short codes to full party names
  const partyMap: { [key: string]: string } = {
    R: "Republican",
    D: "Democratic",
    I: "Independent",
  };

  // Locally filter leadership
  function filterLeadership(data: LeadershipDTO[]) {
    return data.filter((item) => {
      // House or Senate
      const c =
          item.person.currentDistrict !== null
              ? "House of Representatives"
              : "Senate";

      if (filters.chamber !== "all" && filters.chamber !== c) {
        return false;
      }

      // Party
      const leaderParty = partyMap[item.person.partyMembership || ""] || "Unknown";
      if (filters.parties.length > 0 && !filters.parties.includes(leaderParty)) {
        return false;
      }

      // Status
      // If item.currentLeader === "true" => "Incumbent", else "Former Member"
      const st = item.currentLeader === "true" ? "Incumbent" : "Former Member";
      if (filters.status.length > 0 && !filters.status.includes(st)) {
        return false;
      }

      return true;
    });
  }

  // Locally filter the already-fetched search results
  function filterSearchResults(data: PersonDTO[]) {
    return data.filter((p) => {
      // Chamber
      const c = localChamber(p);
      // Match "House of Representatives", "Senate", or "Unknown"
      if (filters.chamber !== "all" && filters.chamber !== c) {
        return false;
      }

      // Party
      const fullParty = partyMap[p.partyMembership || ""] || "Unknown";
      if (filters.parties.length > 0 && !filters.parties.includes(fullParty)) {
        return false;
      }

      // Status
      const st = p.currentMember === "Yes" ? "Incumbent" : "Former Member";
      if (filters.status.length > 0 && !filters.status.includes(st)) {
        return false;
      }

      return true;
    });
  }

  // The final array we will render in the "search results" section
  const localFilteredSearch = pagedResults
      ? filterSearchResults(pagedResults.content)
      : [];

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
              Track congressional records, sponsored bills, legislative impact of current and past Congress members.
            </p>

            {/* Search Form */}
            <form onSubmit={handleSearch} className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    type="search"
                    placeholder="Search by name, state, or party..."
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
              {/* If searching, show local-filtered search results */}
              {isSearching && (
                  <>
                    {loading ? (
                        <div className="text-center text-muted-foreground py-8">Loading...</div>
                    ) : pagedResults && localFilteredSearch.length === 0 ? (
                        <div className="text-center text-muted-foreground py-8">
                          No politicians found matching your criteria
                        </div>
                    ) : pagedResults && localFilteredSearch.length > 0 ? (
                        <>
                          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
                            {localFilteredSearch.map((politician) => {
                              const term = getMostRecentTerm(politician.termList);
                              const chamber = term?.chamber || "Unknown";
                              const districtDisplay = getDistrictDisplay(term);
                              const partyFull =
                                  partyMap[politician.partyMembership || ""] || "Unknown";
                              const status =
                                  politician.currentMember === "Yes" ? "Incumbent" : "Former Member";

                              return (
                                  <PoliticianCard
                                      key={politician.personId}
                                      name={politician.fullName || "Unknown"}
                                      state={term?.stateNm || politician.state || "Unknown"}
                                      party={partyFull}
                                      district={districtDisplay}
                                      imageUrl={politician.imageUrl || backupImg}
                                      status={status}
                                      chamber={chamber}
                                      personId={politician.personId}
                                  />
                              );
                            })}
                          </div>

                          {/* Pagination Controls */}
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

              {/* If not searching, or no search results, show leadership */}
              {showLeadership && (
                  <>
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
                        (() => {
                          const filteredLead = filterLeadership(leadership);

                          const houseLeaders = filteredLead.filter(
                              (l) => l.person.currentDistrict !== null
                          );
                          const senateLeaders = filteredLead.filter(
                              (l) => l.person.currentDistrict === null
                          );

                          const houseRepublican = houseLeaders.filter(
                              (l) => l.person.partyMembership === "R"
                          );
                          const houseDemocratic = houseLeaders.filter(
                              (l) => l.person.partyMembership === "D"
                          );

                          const senateRepublican = senateLeaders.filter(
                              (l) => l.person.partyMembership === "R"
                          );
                          const senateDemocratic = senateLeaders.filter(
                              (l) => l.person.partyMembership === "D"
                          );

                          return (
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
                          );
                        })()
                    )}
                  </>
              )}
            </main>
          </div>
        </div>
      </div>
  );
}