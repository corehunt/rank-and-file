"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
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
import backupImg from "@/app/assets/backup.png"

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

interface LeadershipDTO {
  leadershipId: string;
  leadershipType: string;
  person: PersonSummaryDTO;
}

export default function PoliticiansPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: "all",
    parties: [] as string[],
    status: [] as string[],
  });

  const [politicians, setPoliticians] = useState<PersonDTO[]>([]);
  const [filteredPoliticians, setFilteredPoliticians] = useState<PersonDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  // --- New State for fetched leadership ---
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

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setHasSearched(true);
    fetchPoliticians();
  };

  const fetchPoliticians = () => {
    setLoading(true);
    const encodedSearchTerm = encodeURIComponent(searchQuery.trim());
    const url = `/api/politicians/${encodedSearchTerm}`;

    fetch(url)
        .then((res) => {
          if (!res.ok) {
            throw new Error(`Error fetching politicians: ${res.statusText}`);
          }
          return res.json();
        })
        .then((data: PersonDTO[]) => {
          setPoliticians(data);
          setFilteredPoliticians(data);
          setLoading(false);
        })
        .catch((error) => {
          console.error("Error fetching politicians:", error);
          setLoading(false);
        });
  };

  useEffect(() => {
    let filtered = politicians;

    // Chamber Filter
    if (filters.chamber !== "all") {
      filtered = filtered.filter((politician) => {
        if (!politician.termList || politician.termList.length === 0) return false;
        const sortedTerms = [...politician.termList].sort((a, b) => b.startYr - a.startYr);
        const recentTerm = sortedTerms[0];
        return recentTerm.chamber === filters.chamber;
      });
    }

    // Party Filter
    if (filters.parties.length > 0) {
      filtered = filtered.filter((politician) => {
        const partyMap: { [key: string]: string } = {
          R: "Republican",
          D: "Democratic",
          I: "Independent",
        };
        const partyName = partyMap[politician.partyMembership || ""] || "Unknown";
        return filters.parties.includes(partyName);
      });
    }

    // Status Filter
    if (filters.status.length > 0) {
      filtered = filtered.filter((politician) => {
        const status =
            politician.currentMember === "Yes" ? "Incumbent" : "Former Member";
        return filters.status.includes(status);
      });
    }

    setFilteredPoliticians(filtered);
  }, [filters, politicians]);

  /**
   * Show leadership if no search has been done OR
   * if searched but no results & not currently loading
   */
  const showLeadership =
      !hasSearched || (hasSearched && filteredPoliticians.length === 0 && !loading);

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

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
            <h1 className="text-2xl sm:text-3xl font-bold mb-4">U.S. Politicians</h1>
            <p className="text-base sm:text-lg text-muted-foreground mb-6">
              Track voting records, sponsored bills, and financial connections of current and past Congress members.
            </p>
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

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <aside className="hidden lg:block lg:col-span-1">
              <PoliticianFilters filters={filters} onFilterChange={handleFilterChange} />
            </aside>

            <main className="lg:col-span-3 space-y-8">
              {/* Politicians Results */}
              {hasSearched && (
                  <>
                    {loading ? (
                        <div className="text-center text-muted-foreground py-8">Loading...</div>
                    ) : filteredPoliticians.length === 0 ? (
                        <div className="text-center text-muted-foreground py-8">
                          No politicians found matching your criteria
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
                          {filteredPoliticians.map((politician) => {
                            let chamber = "Unknown";
                            if (politician.termList && politician.termList.length > 0) {
                              const sortedTerms = [...politician.termList].sort(
                                  (a, b) => b.startYr - a.startYr
                              );
                              const recentTerm = sortedTerms[0];
                              chamber = recentTerm.chamber || "Unknown";
                            }

                            const isSenator = chamber === "Senate";
                            let districtDisplay = "";
                            if (!isSenator) {
                              if (politician.currentDistrict !== null) {
                                districtDisplay = `District ${politician.currentDistrict}`;
                              } else {
                                districtDisplay = "District Unknown";
                              }
                            }

                            const partyMap: { [key: string]: string } = {
                              R: "Republican",
                              D: "Democratic",
                              I: "Independent",
                            };
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
                    )}
                  </>
              )}

              {/* Leadership Sections - Only show if no successful search results */}
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
                                    title="Democratic Leadership"
                                    leaders={senateDemocratic}
                                />
                              </div>
                              <div className="bg-muted/50 rounded-lg p-6">
                                <LeadershipSection
                                    title="Republican Leadership"
                                    leaders={senateRepublican}
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