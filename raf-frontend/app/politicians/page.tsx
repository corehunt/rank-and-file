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
  // Add other fields as necessary
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

export default function PoliticiansPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    chamber: "all",
    parties: [] as string[],
    status: [] as string[], // No default status filter
  });
  const [politicians, setPoliticians] = useState<PersonDTO[]>([]);
  const [filteredPoliticians, setFilteredPoliticians] = useState<PersonDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const handleFilterChange = (type: string, value: string | string[]) => {
    setFilters((prev) => ({
      ...prev,
      [type]: value,
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
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
          console.log("Data received from API:", data); // Optional: For debugging
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
        const sortedTerms = [...politician.termList].sort(
            (a, b) => b.startYr - a.startYr
        );
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
        const partyName =
            partyMap[politician.partyMembership || ""] || "Unknown";
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

  return (
      <div className="min-h-screen bg-muted/30">
        <div className="bg-background border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
            <h1 className="text-2xl sm:text-3xl font-bold mb-4">
              U.S. Politicians
            </h1>
            <p className="text-base sm:text-lg text-muted-foreground mb-6">
              Track voting records, sponsored bills, and financial connections of
              current and past Congress members.
            </p>
            <form onSubmit={handleSearch} className="flex gap-4">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                    type="search"
                    placeholder="Search by name, state, or district..."
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
                <Button variant="outline" className="w-full">
                  Filters
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-[300px] sm:w-[400px]">
                <SheetHeader>
                  <SheetTitle>Filter Politicians</SheetTitle>
                </SheetHeader>
                <div className="mt-6">
                  <PoliticianFilters
                      filters={filters}
                      onFilterChange={handleFilterChange}
                  />
                </div>
              </SheetContent>
            </Sheet>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            <aside className="hidden lg:block lg:col-span-1">
              <PoliticianFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
              />
            </aside>
            <main className="lg:col-span-3">
              {filteredPoliticians.length === 0 ? (
                  <div className="text-center text-muted-foreground py-8">
                    No politicians found matching your criteria
                  </div>
              ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">
                    {filteredPoliticians.map((politician) => {
                      // Extract the chamber from the most recent term
                      let chamber = "Unknown";
                      if (
                          politician.termList &&
                          politician.termList.length > 0
                      ) {
                        const sortedTerms = [...politician.termList].sort(
                            (a, b) => b.startYr - a.startYr
                        );
                        const recentTerm = sortedTerms[0];
                        chamber = recentTerm.chamber || "Unknown";
                      }

                      // Determine if the politician is a Senator
                      const isSenator = chamber === "Senate";

                      // For Senators, we omit the district line
                      let districtDisplay = "";
                      if (!isSenator) {
                        // For House members, display the district
                        if (politician.currentDistrict !== null) {
                          districtDisplay = `District ${politician.currentDistrict}`;
                        } else {
                          districtDisplay = "District Unknown";
                        }
                      }

                      // Map party code to full name
                      const partyMap: { [key: string]: string } = {
                        R: "Republican",
                        D: "Democratic",
                        I: "Independent",
                      };
                      const partyName =
                          partyMap[politician.partyMembership || ""] || "Unknown";

                      // Determine status
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
                              imageUrl={
                                  politician.imageUrl || "/default-image.jpg"
                              }
                              status={status}
                              chamber={chamber}
                           personId={politician.personId}/>
                      );
                    })}
                  </div>
              )}
            </main>
          </div>
        </div>
      </div>
  );
}