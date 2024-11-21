"use client";

import { useState, useEffect } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import DonorCard from "@/components/donors/donor-card";
import DonorFilters from "@/components/donors/donor-filters";
import { 
  Sheet, 
  SheetContent, 
  SheetTrigger,
  SheetHeader,
  SheetTitle 
} from "@/components/ui/sheet";

const SAMPLE_DONORS = [
  {
    id: 1,
    name: "Tech Innovation PAC",
    type: "Political Action Committee",
    totalDonations: "$2.5M",
    topRecipients: [
      { name: "Jane Smith", amount: "$150,000" },
      { name: "John Doe", amount: "$125,000" }
    ],
    industries: ["Technology", "Communications"],
    donationRange: "over500k"
  },
  {
    id: 2,
    name: "Global Energy Corp",
    type: "Corporation",
    totalDonations: "$1.8M",
    topRecipients: [
      { name: "John Doe", amount: "$200,000" },
      { name: "Jane Smith", amount: "$175,000" }
    ],
    industries: ["Energy", "Infrastructure"],
    donationRange: "100k-500k"
  }
];

export default function DonorsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    type: "all",
    industries: [] as string[],
    donationRanges: [] as string[]
  });
  const [filteredDonors, setFilteredDonors] = useState(SAMPLE_DONORS);

  const handleFilterChange = (type: string, value: string | string[]) => {
    setFilters(prev => ({
      ...prev,
      [type]: value
    }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
  };

  useEffect(() => {
    let filtered = SAMPLE_DONORS;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(donor =>
        donor.name.toLowerCase().includes(query) ||
        donor.type.toLowerCase().includes(query) ||
        donor.industries.some(industry => industry.toLowerCase().includes(query))
      );
    }

    if (filters.type !== "all") {
      filtered = filtered.filter(donor => 
        donor.type.toLowerCase() === filters.type.toLowerCase()
      );
    }

    if (filters.industries.length > 0) {
      filtered = filtered.filter(donor =>
        donor.industries.some(industry => filters.industries.includes(industry))
      );
    }

    if (filters.donationRanges.length > 0) {
      filtered = filtered.filter(donor =>
        filters.donationRanges.includes(donor.donationRange)
      );
    }

    setFilteredDonors(filtered);
  }, [searchQuery, filters]);

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
          <h1 className="text-2xl sm:text-3xl font-bold mb-4">Political Donors</h1>
          <p className="text-base sm:text-lg text-muted-foreground mb-6">
            Track and analyze campaign contributions and donor networks.
          </p>
          <form onSubmit={handleSearch} className="flex gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                type="search"
                placeholder="Search by donor name, type, or industry..."
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
                <SheetTitle>Filter Donors</SheetTitle>
              </SheetHeader>
              <div className="mt-6">
                <DonorFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
                />
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          <aside className="hidden lg:block lg:col-span-1">
            <DonorFilters
              filters={filters}
              onFilterChange={handleFilterChange}
            />
          </aside>
          <main className="lg:col-span-3">
            {filteredDonors.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                No donors found matching your criteria
              </div>
            ) : (
              <div className="grid gap-6">
                {filteredDonors.map((donor) => (
                  <DonorCard
                    key={donor.id}
                    name={donor.name}
                    type={donor.type}
                    totalDonations={donor.totalDonations}
                    topRecipients={donor.topRecipients}
                    industries={donor.industries}
                  />
                ))}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}