"use client";

import { Card, CardHeader, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Checkbox } from "@/components/ui/checkbox";

interface PoliticianFiltersProps {
  filters: {
    chamber: string;
    parties: string[];
    status: string[];
  };
  onFilterChange: (type: string, value: string | string[]) => void;
}

export default function PoliticianFilters({
                                            filters,
                                            onFilterChange,
                                          }: PoliticianFiltersProps) {
  return (
      <Card className="sticky top-4">
        <CardHeader>
          <h2 className="text-lg font-semibold">Search Result Filters</h2>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Chamber Filter */}
          <div className="space-y-4">
            <h3 className="font-medium">Chamber</h3>
            <RadioGroup
                value={filters.chamber}
                onValueChange={(value) => onFilterChange("chamber", value)}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="all" id="all" />
                <Label htmlFor="all">All Chambers</Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="House of Representatives" id="house" />
                <Label htmlFor="house">House of Representatives</Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="Senate" id="senate" />
                <Label htmlFor="senate">Senate</Label>
              </div>
            </RadioGroup>
          </div>

          <Separator />

          {/* Party Filter */}
          <div className="space-y-4">
            <h3 className="font-medium">Party</h3>
            <div className="space-y-2">
              {["Democratic", "Republican", "Independent"].map((party) => (
                  <div key={party} className="flex items-center space-x-2">
                    <Checkbox
                        id={party}
                        checked={filters.parties.includes(party)}
                        onCheckedChange={(checked) => {
                          const newParties = checked
                              ? [...filters.parties, party]
                              : filters.parties.filter((p) => p !== party);
                          onFilterChange("parties", newParties);
                        }}
                    />
                    <Label htmlFor={party}>{party}</Label>
                  </div>
              ))}
            </div>
          </div>

          <Separator />

          {/* Status Filter */}
          <div className="space-y-4">
            <h3 className="font-medium">Status</h3>
            <div className="space-y-2">
              {["Incumbent", "Former Member"].map((statusOption) => (
                  <div key={statusOption} className="flex items-center space-x-2">
                    <Checkbox
                        id={statusOption}
                        checked={filters.status.includes(statusOption)}
                        onCheckedChange={(checked) => {
                          const newStatus = checked
                              ? [...filters.status, statusOption]
                              : filters.status.filter((s) => s !== statusOption);
                          onFilterChange("status", newStatus);
                        }}
                    />
                    <Label htmlFor={statusOption}>{statusOption}</Label>
                  </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
  );
}