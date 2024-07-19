import { GetServerSideProps } from 'next';
import Image from 'next/image';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useState } from 'react';
import PropTypes from 'prop-types';

interface Politician {
    personId: string;
    firstName: string;
    midName?: string;
    lastName: string;
    fullName: string;
    birthDate?: string;
    deathDate?: string;
    website?: string;
    officeLocLine1?: string;
    officeLocLine2?: string;
    phoneNo?: string;
    state?: string;
    currentDistrict?: number;
    biography?: string;
    email?: string;
    imageUrl?: string;
    imgAttribution?: string;
    partyMembership?: string;
    partyStartYr?: number;
}

interface PoliticianPageProps {
    politician?: Politician;
    error?: string;
}

function TabPanel(props: { children?: React.ReactNode; index: any; value: any }) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box p={3}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.any.isRequired,
    value: PropTypes.any.isRequired,
};

function a11yProps(index: any) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

const PoliticianPage: React.FC<PoliticianPageProps> = ({ politician, error }) => {
    const [value, setValue] = useState(0);

    const handleChange = (event: React.ChangeEvent<{}>, newValue: number) => {
        setValue(newValue);
    };

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="container mx-auto p-4">
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                {politician?.imageUrl ? (
                    <Image
                        src={politician.imageUrl}
                        alt={politician.firstName}
                        width={200}
                        height={200}
                        onError={(e) => {
                            e.currentTarget.src = '/fallback-image.png';
                        }}
                    />
                ) : (
                    <Image src="/fallback-image.png" alt="fallback image" width={200} height={200} />
                )}
                <Typography variant="h4">{politician?.fullName}</Typography>
                <Typography variant="subtitle1" style={{ fontStyle: 'italic' }}>
                    {politician?.partyMembership} {politician?.state} {politician?.currentDistrict}
                </Typography>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '20px' }}>
                <div>
                    <Typography variant="body1"><strong>Office:</strong> {politician?.officeLocLine1}</Typography>
                    <Typography variant="body1">{politician?.officeLocLine2}</Typography>
                    <Typography variant="body1"><strong>Phone:</strong> {politician?.phoneNo}</Typography>
                </div>
                <div>
                    <Typography variant="body1"><strong>Chamber:</strong> House of Representatives</Typography>
                    <Typography variant="body1"><strong>Years active:</strong> Member Since {politician?.partyStartYr}</Typography>
                    <Typography variant="body1"><strong>Current Member:</strong> Yes</Typography>
                </div>
            </div>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', textAlign: 'center' }}>
                <Tabs value={value} onChange={handleChange} aria-label="basic tabs example" centered>
                    <Tab label="Biography" {...a11yProps(0)} />
                    <Tab label="Sponsored Legislation" {...a11yProps(1)} />
                    <Tab label="Co-Sponsored Legislation" {...a11yProps(2)} />
                    <Tab label="Campaign Contributions" {...a11yProps(3)} />
                    <Tab label="Voting Record" {...a11yProps(4)} />
                </Tabs>
            </Box>
            <TabPanel value={value} index={0}>
                {politician?.biography}
            </TabPanel>
            <TabPanel value={value} index={1}>
                Sponsored Legislation Content
            </TabPanel>
            <TabPanel value={value} index={2}>
                Co-Sponsored Legislation Content
            </TabPanel>
            <TabPanel value={value} index={3}>
                Campaign Contributions Content
            </TabPanel>
            <TabPanel value={value} index={4}>
                Voting Record Content
            </TabPanel>
        </div>
    );
};

export const getServerSideProps: GetServerSideProps = async (context) => {
    try {
        const { personId } = context.params!;
        const res = await fetch(`http://localhost:3000/api/politician/${personId}`);

        if (!res.ok) {
            throw new Error(`Failed to fetch data. Status: ${res.status}`);
        }

        const politician = await res.json();

        return {
            props: {
                politician,
            },
        };
    } catch (error) {
        console.error('Error fetching data:', error);

        return {
            props: {
                error: 'Failed to fetch data',
            },
        };
    }
};

export default PoliticianPage;
